'''
Created on Apr 11, 2015

@author: jkarnuta
'''
import ConfigParser
import sys
from ConfigParser import DuplicateSectionError, NoSectionError

class ytAudioSettings:
    """
    Initialize the ytAudioSettings class. Automatically loads the default file
    """
    def __init__(self):
        self.rawParser = ConfigParser.RawConfigParser()
        self.itunes_filepath = ""
        self.temp_filepath = ""
        self.settings_file_path = self.getExecResourcesPath()
        #self.settings_file_path = "project_settings.ini"
        self.rawParser.readfp(open(self.settings_file_path, "r"))
        self.sections = self.rawParser.sections()

        return
    
    def getExecResourcesPath(self):
        path = sys.executable.split("/")
        path = path[:-2]
        path.append("Resources")
        path.append("project_settings.ini")
        path = "/".join(path)
        return path
        
    """
    Allows user to load a new settings file. File must have same sections as project_settings.ini
    """
    def load_new(self, new_settings_file):
        self.settings_file_path = new_settings_file
        self.rawParser.read(open(self.settings_file_path, "r"))
        self.sections = self.rawParser.sections()
    
    """
    Allows user to add new section
    """
    def addSection(self, newSectionName):
        try:
            self.rawParser.add_section(newSectionName)
        except DuplicateSectionError:
                print "ERROR: Duplicate Section Detected"
                raise
        self.sections = self.rawParser.sections()
        return
    """
    sets an option to a section. If section must exist (you can add a section via addSection)
    """
    def addOptions(self, section, option,value):
        try:
            self.rawParser.set(section, option, value)
        except NoSectionError:
            print "ERROR: "+section+" does not exist. Add via addSection(newSectionName)"
            raise
    """
    Reads one section and returns the (Name:Value) tuples
    """
    def readSection(self,section):
        return self.rawParser.items(section)
    
    """
    Makes a 2-layer dictionary from the full file.
    E.g. {Section 1: {Option 1: value 1, Option 2: value 2,...}, Section 2: {...},...}
    """
    def get_allItems(self):
        newDict = {}
        for section in self.sections:
            newDict[section] = dict(self.readSection(section))
        return newDict
    """
    Sets an item
    """
    def set_item(self, section, option, value):
        self.rawParser.set(section, option, value)
    """
    writes to file
    """
    def write_out(self):
        print "SETTINGS - writing to: "+self.settings_file_path
        writeFile = open(self.settings_file_path, 'w')
        self.rawParser.write(writeFile)

if __name__ == "__main__":
    newParser = ytAudioSettings()
    print newParser.get_allItems()
   
   
    
    