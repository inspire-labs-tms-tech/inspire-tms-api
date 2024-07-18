import xml.etree.ElementTree as ET

def add_distribution_management(xml_path):
    tree = ET.parse(xml_path)
    root = tree.getroot()

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
    project = root.find('./')
    project.append(distribution_management)

    # Write the modified XML back to the file
    tree.write(xml_path, encoding='utf-8', xml_declaration=True)

# Specify the path to the XML file
xml_path = './gen/pom.xml'
add_distribution_management(xml_path)
