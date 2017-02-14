# BCOffCampus
######David Schmetterling  
###Android application to assist BC students in finding off campus housing  

**Overview**  
“BC Off Campus Housing” is an app I created to assist Boston College students that are seeking off campus housing. Here at BC, most students are not given all four years of housing and are forced to go through devious real estate agents. This is a very tedious and unpleasant experience for the majority of students that go through it.  

**Features**  
  1. Multiple Activities:
    * The main window is a list view of the properties available to rent. This custom list view contains an image of the property, the address, and the (monthly) price. Once a property is clicked on, a full page will open with a complete description of the listing, the address, a full list of features, a map, and other useful links. Tab fragments are used to switch between the list view and map view, which are loaded asynchronously.  
  2. Map:  
    * The map shows the user’s current location and nearby properties through visual pins using Google Maps API.  
  3. JSON data (or real impressive scraping with regular expressions):  
    * Most listing websites (like Zillow) do not offer APIs that have JSON data. Instead, I used regex and knowledge of HTML structure to sift through and pick out important features. The original website: http://www.myapartmentmap.com/list/colleges/ma/boston_college/ no longer exists and instead has changed to: https://www.abodo.com/boston-ma/boston-college-apartments/campus?page=1.  
  4. Asynch/Threading:  
    * The “refresh listings” button updates the property listings by spawning a background thread to refresh the data (with a modal graphic to make the user wait).  
  5. SQL database:  
    * The property listings are stored in an SQL database for easy offline access. They can be manually refreshed by clicking the refresh button.  

See the app in action: https://youtu.be/m076n-dyJHg
