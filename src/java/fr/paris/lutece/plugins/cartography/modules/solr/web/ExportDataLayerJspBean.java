/*
 * Copyright (c) 2002-2023, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */

package fr.paris.lutece.plugins.cartography.modules.solr.web;

import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.business.file.File;
import fr.paris.lutece.portal.business.file.FileHome;
import fr.paris.lutece.portal.business.physicalfile.PhysicalFile;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.file.FileService;
import fr.paris.lutece.portal.service.file.IFileStoreServiceProvider;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.util.url.UrlItem;
import fr.paris.lutece.util.html.AbstractPaginator;

import java.util.Comparator;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.paris.lutece.plugins.carto.business.Basemap;
import fr.paris.lutece.plugins.carto.business.BasemapHome;
import fr.paris.lutece.plugins.carto.business.DataLayer;
import fr.paris.lutece.plugins.carto.business.DataLayerHome;
import fr.paris.lutece.plugins.carto.business.DataLayerMapTemplate;
import fr.paris.lutece.plugins.carto.business.DataLayerMapTemplateHome;
import fr.paris.lutece.plugins.carto.business.DataLayerType;
import fr.paris.lutece.plugins.carto.business.DataLayerTypeHome;
import fr.paris.lutece.plugins.cartography.modules.solr.business.TemporaryFileExtract;
import fr.paris.lutece.plugins.cartography.modules.solr.service.CartographyService;
import fr.paris.lutece.plugins.filegenerator.business.TemporaryFile;
import fr.paris.lutece.plugins.filegenerator.service.TemporaryFileGeneratorService;
import fr.paris.lutece.plugins.leaflet.business.GeolocItem;
import fr.paris.lutece.plugins.leaflet.business.GeolocItemPolygon;
import fr.paris.lutece.plugins.leaflet.service.IconService;
import fr.paris.lutece.plugins.search.solr.business.SolrSearchEngine;
import fr.paris.lutece.plugins.search.solr.business.SolrSearchResult;
import fr.paris.lutece.plugins.search.solr.indexer.SolrItem;

/**
 * This class provides the user interface to manage Basemap features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageExportDataLayer.jsp", controllerPath = "jsp/admin/plugins/cartosolr/", right = "CARTO_EXPORT_SOLR" )
public class ExportDataLayerJspBean extends AbstractManageCartoJspBean<Integer, Basemap>
{
    // Templates
    private static final String TEMPLATE_EXPORT_BASEMAPS = "/admin/plugins/cartosolr/export_datalayer.html";
    private static final String TEMPLATE_CREATE_BASEMAP = "/admin/plugins/cartosolr/create_basemap.html";
    
    private static final String PARAMETER_SOLR_GEOJSON = "DataLayer_text";
    //private static final String DIRECTORY_FILENAME = "/tmp/";
    private static final String DIRECTORY_FILENAME =  System.getProperty( "java.io.tmpdir" );

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_BASEMAPS = "carto.manage_basemaps.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_BASEMAP = "carto.create_basemap.pageTitle";

    // Markers
    private static final String MARK_BASEMAP_LIST = "basemap_list";
    private static final String MARK_BASEMAP = "basemap";
    private static final String MARK_URL_DOWNLOAD = "urlFileDownload";

    private static final String JSP_MANAGE_BASEMAPS = "jsp/admin/plugins/cartosolr/ManageExportDataLayer.jsp";

    // Views
    private static final String VIEW_EXPORT_DATALAYER = "manageBasemaps";
    private static final String VIEW_CREATE_BASEMAP = "createBasemap";
    // Actions
    private static final String ACTION_EXPORT_DATALAYER = "exportDataLayer";
    
    private static final String ACTION_CREATE_BASEMAP = "createBasemap";
    private static final String MARK_REF_DATA_LAYER = "reflist_data_layer";

    // Session variable to store working values
    private Basemap _basemap;
    private List<Integer> _listIdBasemaps;
    private String _strUrlDownload;

    /**
     * Build the Manage View
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_EXPORT_DATALAYER, defaultView = true )
    public String getManageBasemaps( HttpServletRequest request )
    {
        _basemap = null;
        _strUrlDownload = ( _strUrlDownload != null ) ? _strUrlDownload : new String( );

        if ( request.getParameter( AbstractPaginator.PARAMETER_PAGE_INDEX ) == null || _listIdBasemaps.isEmpty( ) )
        {
            _listIdBasemaps = BasemapHome.getIdBasemapsList( );
        }

        Map<String, Object> model = getPaginatedListModel( request, MARK_BASEMAP_LIST, _listIdBasemaps, JSP_MANAGE_BASEMAPS );

        model.put( MARK_REF_DATA_LAYER, DataLayerHome.getDataLayersReferenceList() );
        model.put( MARK_URL_DOWNLOAD , _strUrlDownload );
        
        return getPage( PROPERTY_PAGE_TITLE_MANAGE_BASEMAPS, TEMPLATE_EXPORT_BASEMAPS, model );
    }

    /**
     * Get Items from Ids list
     * 
     * @param listIds
     * @return the populated list of items corresponding to the id List
     */
    @Override
    List<Basemap> getItemsFromIds( List<Integer> listIds )
    {
        List<Basemap> listBasemap = BasemapHome.getBasemapsListByIds( listIds );

        // keep original order
        return listBasemap.stream( ).sorted( Comparator.comparingInt( notif -> listIds.indexOf( notif.getId( ) ) ) ).collect( Collectors.toList( ) );
    }

    /**
     * reset the _listIdBasemaps list
     */
    public void resetListId( )
    {
        _listIdBasemaps = new ArrayList<>( );
    }

    /**
     * Returns the form to create a basemap
     *
     * @param request
     *            The Http request
     * @return the html code of the basemap form
     */
    @View( VIEW_CREATE_BASEMAP )
    public String getCreateBasemap( HttpServletRequest request )
    {
        _basemap = ( _basemap != null ) ? _basemap : new Basemap( );

        Map<String, Object> model = getModel( );
        model.put( MARK_BASEMAP, _basemap );
        model.put( SecurityTokenService.MARK_TOKEN, SecurityTokenService.getInstance( ).getToken( request, ACTION_CREATE_BASEMAP ) );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_BASEMAP, TEMPLATE_CREATE_BASEMAP, model );
    }

    /**
     * Process the data capture form of a new basemap
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( ACTION_EXPORT_DATALAYER )
    public String doCreateBasemap( HttpServletRequest request ) throws AccessDeniedException
    {
        int nDataLayer = Integer.parseInt( request.getParameter("layer_type") );
    	DataLayer dataLayer = DataLayerHome.findByPrimaryKey( nDataLayer ).get( );
    	getLayerSolr( dataLayer );

        return redirectView( request, VIEW_EXPORT_DATALAYER );
    }

    public String getLayerSolr( DataLayer datalayer )
    {
    	
    	SolrSearchEngine engine = SolrSearchEngine.getInstance( );

        List<String> lstGeoJSONCoordinates = new ArrayList<>( );
        
        //int nlimit = AppPropertiesService.getPropertyInt( PROPERTY_LIMIT_RESULT_SOLR, 100 );
        int nlimit = 100;

        List<SolrSearchResult> listResultsGeoloc = engine.getGeolocSearchResults( PARAMETER_SOLR_GEOJSON + ":" + datalayer.getSolrTag( ), null, nlimit );
        lstGeoJSONCoordinates = getGeolocModel( listResultsGeoloc, datalayer ) ;

        FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(DIRECTORY_FILENAME + "/"+ "ExtractCartoLayer.json" );
			fileWriter.write( "{\n"
					+ "   \"type\":\"FeatureCollection\",\n"
					+ "   \"name\":\" "+ datalayer.getTitle() +" \",\n"
					+ "   \"crs\":{\n"
					+ "      \"type\":\"name\",\n"
					+ "      \"properties\":{\n"
					+ "         \"name\":\"urn:ogc:def:crs:OGC:1.3:CRS84\"\n"
					+ "      }\n"
					+ "   },\n"
					+ "   \"features\":[" );
				for ( int i = 0; i < lstGeoJSONCoordinates.size(); i++ )
				{
					if ( i < lstGeoJSONCoordinates.size() - 1 )
						fileWriter.write( lstGeoJSONCoordinates.get( i ) + "," + System.lineSeparator());
					else
						fileWriter.write( lstGeoJSONCoordinates.get( i ) + System.lineSeparator());
		        }
			 fileWriter.write( "] }" );
		        fileWriter.close();
		        
		        /*
		        java.io.File file = new java.io.File( DIRECTORY_FILENAME + datalayer.getTitle() + ".txt" );
		        byte[] fileContent = Files.readAllBytes(file.toPath());
		        
		        
		        File fileLutece = new File( );
		        PhysicalFile physicalFile = new PhysicalFile( );
		        physicalFile.setValue( fileContent );
		        fileLutece.setPhysicalFile( physicalFile );
		        fileLutece.setSize( (int) file.length( ) );
		        fileLutece.setOrigin( datalayer.getTitle() );
		        fileLutece.setMimeType("application/json");
		        fileLutece.setTitle( datalayer.getTitle() );
		        //fileLutece.setOrigin( getName( ) );
		        
		        //int nFileId = FileHome.create( fileLutece );
		        
		        IFileStoreServiceProvider fileStoreService = FileService.getInstance( ).getFileStoreServiceProvider( );
		        fileLutece.setFileKey( fileStoreService.storeFile(fileLutece) );  ;
		        _strUrlDownload = fileStoreService.getFileDownloadUrlBO( fileLutece.getFileKey() );
		        //_strUrlDownload = fileStoreService.getFileDownloadUrlBO( String.valueOf( nFileId )  );
		        */
		        
		        TemporaryFileExtract extractFile = new TemporaryFileExtract();
		        TemporaryFileGeneratorService.getInstance().generateFile( extractFile, getUser( ) );
		       
		        
		        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       
        return _strUrlDownload;
    }
    
    
    /**
     * Returns a model with points data from a geoloc search
     * 
     * @param listResultsGeoloc
     *            the result of a search
     * @return the model
     */
    public static List<String> getGeolocModel( List<SolrSearchResult> listResultsGeoloc, DataLayer datalayer )
    {
        List<String> lstGeoJSONCoordinates = new ArrayList<>( );

        for ( SolrSearchResult result : listResultsGeoloc )
        {
            Map<String, Object> dynamicFields = result.getDynamicFields( );

            for ( Entry<String, Object> entry : dynamicFields.entrySet( ) )
            {

                if ( !entry.getKey( ).endsWith( SolrItem.DYNAMIC_GEOJSON_FIELD_SUFFIX ) )
                {
                    continue;
                }
                HashMap<String, Object> h = new HashMap<>( );
                String strJson = (String) entry.getValue( );
                
                lstGeoJSONCoordinates.add( strJson );
            }
           
                
        }
        return lstGeoJSONCoordinates;
    }
    
    
}
