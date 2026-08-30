# Module cartography solr

## Introduction
The cartography solr module is used to store and to access geographic data for display in the front-office.

## Configuration
With the default security headers settings, maps cannot be displayed correctly. For each page containing a map, you must assign values to two security headers to restore the map display. Here’s how to do it:

- On the page containing the map, in the displayed URL, extract the string that begins with /jsp/xxx (Example: /jsp/site/Portal.jsp?page=coordinate&view=manageCoordonnees).
If this part contains one or more identifiers that are likely to change (numeric form identifiers, for example), replace them with an asterisk so that the maps are displayed consistently.


- Modifying the value of **Content-Security-Policy** header :
    - In the back office, go to System > Security Header Management
    - Click the “View more” button for the Content-Security-Policy header
    - On the management page, click “Create an exception”
    - In the **Value** field, add the following value : default-src 'self'; script-src * 'unsafe-inline' 'unsafe-eval'; style-src * 'unsafe-inline'; img-src * data: blob:; connect-src * blob:; object-src 'none'; font-src * data:;form-action 'self'; frame-ancestors 'self'; frame-src *; upgrade-insecure-requests
    - In the URL pattern field, add the string starting with /jsp/xxx that you retrieved earlier
    - Click “Create” and then “Back”


- Modifying the value of **Cross-Origin-Embedder-Policy** header :
    - Perform the same steps as for the previous header, except that add the following value for **Value** field : unsafe-none

You should now be able to see your maps.

## Usage
The features available in lutece-carto-module-cartography-solr module are as follows:
- Indexing of entities corresponding to geographic coordinates (SolrCoordinateIndexer class)
- Adding markers to a map and searching by geographic coordinates (coordinate jsp)
- Exporting data layers created via the Solr plugin to a JSON file stored in the database (filegen_temporary_file table)