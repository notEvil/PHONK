import javalang.ast as j_ast
import javalang.parse as j_parse
import javalang.tree as j_tree
import argparse
import dataclasses
import pathlib


_TYPE_NAME_MAP = {
    "NativeObject": "object",
    "Object": "any",
    "ReturnInterface": "Function",
    "String": "string",
}


parser = argparse.ArgumentParser()

parser.add_argument("path", help="Path to PHONK repository")

arguments = parser.parse_args()
arguments.path = pathlib.Path(arguments.path)


# get entry points

_ = "PHONK-android/phonk_apprunner/src/main/java/io/phonk/runner/apprunner/AppRunner.java"
with open(pathlib.Path(arguments.path, _), "r") as file:
    document_node = j_parse.parse(file.read())


# find class node
for _, node in document_node:
    match node:
        case j_tree.ClassDeclaration(name="AppRunner"):
            class_node = node
            break
else:
    raise Exception


# get types
type_nodes = {}

for node in class_node.body:
    match node:
        case j_tree.FieldDeclaration(
            type=j_tree.ReferenceType(), declarators=[j_tree.VariableDeclarator()]
        ):
            type_nodes[node.declarators[0].name] = node.type


# find method node
for node in class_node.body:
    match node:
        case j_tree.MethodDeclaration(name="initInterpreter"):
            method_node = node
            break
else:
    raise Exception


entries = {}

for node in method_node.body:
    match node:
        case j_tree.StatementExpression(
            expression=j_tree.MethodInvocation(
                qualifier="interp",
                member="addJavaObjectToJs",
                arguments=[j_tree.Literal(), j_tree.MemberReference()],
            )
        ):
            literal_value = eval(node.expression.arguments[0].value)
            if isinstance(literal_value, str):
                entries[literal_value] = type_nodes[node.expression.arguments[1].member]

#

# get declarations


def get_file_paths(path):
    for path in path.iterdir():
        if path.is_dir():
            yield from get_file_paths(path)

        else:
            yield path


@dataclasses.dataclass
class _Field:
    type_node: j_ast.Node


@dataclasses.dataclass
class _Argument:
    type_node: j_ast.Node
    is_args: bool


@dataclasses.dataclass
class _Method:
    return_type: j_ast.Node | None
    arguments: dict[str, _Argument]


@dataclasses.dataclass
class _Interface:
    extends: j_ast.Node | None
    attributes: list[tuple[str, _Field | _Method]]


@dataclasses.dataclass
class _Class(_Interface):
    implements: list[j_ast.Node]


declarations = {}

_ = "PHONK-android/phonk_apprunner/src/main/java/io/phonk/runner/apprunner/api"
for path in get_file_paths(pathlib.Path(arguments.path, _)):
    if path.suffix != ".java":
        continue

    with open(path, "r") as file:
        document_node = j_parse.parse(file.read())

    # find class or interface node
    for _, node in document_node:
        match node:
            case j_tree.ClassDeclaration():
                parent_node = node
                break

            case j_tree.InterfaceDeclaration():
                parent_node = node
                break
    else:
        raise Exception

    attributes = []

    for node in parent_node.body:
        match node:
            case j_tree.FieldDeclaration(
                annotations=[j_tree.Annotation(name="PhonkField")],
                declarators=[j_tree.VariableDeclarator()],
            ):
                _ = (node.declarators[0].name, _Field(type_node=node.type))
                attributes.append(_)

            case j_tree.MethodDeclaration(
                annotations=[j_tree.Annotation(name="PhonkMethod")]
            ):
                arguments = {}

                for parameter_node in node.parameters:
                    arguments[parameter_node.name] = _Argument(
                        type_node=parameter_node.type, is_args=parameter_node.varargs
                    )

                _ = _Method(return_type=node.return_type, arguments=arguments)
                attributes.append((node.name, _))

    if isinstance(parent_node, j_tree.ClassDeclaration):
        declarations[parent_node.name] = _Class(
            extends=parent_node.extends,
            implements=parent_node.implements,
            attributes=attributes,
        )

    elif isinstance(parent_node, j_tree.InterfaceDeclaration):
        declarations[parent_node.name] = _Interface(
            extends=parent_node.extends, attributes=attributes
        )

    else:
        raise Exception

#

# print declarations


def get_type_string(node):
    if node is None:
        return "void"

    assert isinstance(node, (j_tree.BasicType, j_tree.ReferenceType))

    strings = [_TYPE_NAME_MAP.get(node.name, node.name)]

    if isinstance(node, j_tree.ReferenceType):
        if node.arguments is not None:  # TODO when?
            strings.append("<")

            for type_argument in node.arguments:
                strings.append(get_type_string(type_argument.type))
                strings.append(", ")

            strings.pop()
            strings.append(">")

        if node.sub_type is not None:  # TODO when?
            strings.append(".")
            strings.append(get_type_string(node.sub_type))

    if node.dimensions is not None and len(node.dimensions) != 0:  # TODO when?
        strings.append("[")
        for dimension in node.dimensions:
            assert dimension is None
        strings.append("]")

    return "".join(strings)


for name, declaration in declarations.items():
    _ = "declare {} {}{}{} {{".format(
        {_Class: "class", _Interface: "interface"}[type(declaration)],
        name,
        ""
        if declaration.extends is None
        else (" extends " + get_type_string(declaration.extends)),
        (" implements " + ", ".join(get_type_string(_) for _ in declaration.implements))
        if isinstance(declaration, _Class) and declaration.implements is not None
        else "",
    )
    print(_)

    for name, attribute in declaration.attributes:
        match attribute:
            case _Field():
                print("  {}: {};".format(name, get_type_string(attribute.type_node)))

            case _Method():
                _ = ", ".join(
                    "{}{}: {}{}".format(
                        "..." if argument.is_args else "",
                        name,
                        get_type_string(argument.type_node),
                        "[]" if argument.is_args else "",
                    )
                    for name, argument in attribute.arguments.items()
                )
                print(
                    "  {}({}): {};".format(
                        name, _, get_type_string(attribute.return_type)
                    )
                )

    print("}")


for name, type_node in entries.items():
    print("export var {}: {};".format(name, get_type_string(type_node)))
