import xml.etree.ElementTree as ET
from xml.dom import minidom

def add_distribution_management(xml_path):
    tree = ET.parse(xml_path)
    root = tree.getroot()

    # Define namespaces (if any) and register them
    namespaces = {'': 'http://maven.apache.org/POM/4.0.0'}
    ET.register_namespace('', namespaces[''])

    # Create the new XML structure
    distribution_management = ET.Element('distributionManagement')
    repository = ET.SubElement(distribution_management, 'repository')
    repo_id = ET.SubElement(repository, 'id')
    repo_id.text = 'github'
    name = ET.SubElement(repository, 'name')
    name.text = 'GitHub OWNER Apache Maven Packages'
    url = ET.SubElement(repository, 'url')
    url.text = 'https://maven.pkg.github.com/inspire-labs-tms-tech/inspire-tms-api'

    # Find the project element and add the new XML structure
    project = root.find('{http://maven.apache.org/POM/4.0.0}project')
    if project is None:
        project = root

    project.append(distribution_management)

    # Convert the tree to a string and write it back without XML declaration
    xml_str = ET.tostring(root, encoding='utf-8').decode('utf-8')
    xml_pretty_str = minidom.parseString(xml_str).toprettyxml(indent="  ", encoding='utf-8').decode('utf-8')
    with open(xml_path, 'w', encoding='utf-8') as f:
        f.write(xml_pretty_str)

# Specify the path to the XML file
xml_path = './gen/pom.xml'
add_distribution_management(xml_path)
