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

import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.util.mvc.admin.MVCAdminJspBean;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.web.cdi.mvc.Models;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;


import fr.paris.lutece.plugins.carto.business.BasemapHome;
import fr.paris.lutece.plugins.carto.business.DataLayer;
import fr.paris.lutece.plugins.carto.business.DataLayerHome;
import fr.paris.lutece.plugins.cartography.modules.solr.business.TemporaryFileExtract;
import fr.paris.lutece.plugins.filegenerator.service.TemporaryFileGeneratorService;
import fr.paris.lutece.plugins.search.solr.business.SolrSearchEngine;
import fr.paris.lutece.plugins.search.solr.business.SolrSearchResult;
import fr.paris.lutece.plugins.search.solr.indexer.SolrItem;

/**
 * This class provides the user interface to manage Basemap features ( manage, create, modify, remove )
 */
@RequestScoped
@Named
@Controller( controllerJsp = "ManageExportDataLayer.jsp", controllerPath = "jsp/admin/plugins/cartosolr/", right = "CARTO_EXPORT_SOLR", securityTokenEnabled = true )
public class ExportDataLayerJspBean extends MVCAdminJspBean
{
    // Templates
    private static final String TEMPLATE_EXPORT_DATALAYER = "/admin/plugins/cartosolr/export_datalayer.html";
    
    private static final String PARAMETER_SOLR_GEOJSON = "DataLayer_text";
    private static final String DIRECTORY_FILENAME =  System.getProperty( "java.io.tmpdir" );

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_BASEMAPS = "carto.manage_basemaps.pageTitle";

    // Markers
    private static final String MARK_BASEMAP_LIST = "basemap_list";
    private static final String MARK_REF_DATA_LAYER = "reflist_data_layer";

    // Views
    private static final String VIEW_EXPORT_DATALAYER = "manageBasemaps";
    
    // Actions
    private static final String ACTION_EXPORT_DATALAYER = "exportDataLayer";
    
    @Inject
    private Models model;
    
    @Inject
    private TemporaryFileGeneratorService _temporaryFileGeneratorService;

    /**
     * Build the Export View
     * 
     * @param request
     *            The HTTP request
     * @return The page
     */
    @View( value = VIEW_EXPORT_DATALAYER, defaultView = true )
    public String getExportDataLayer( HttpServletRequest request )
    {
        model.put( MARK_BASEMAP_LIST, BasemapHome.getIdBasemapsList( ) );
        model.put( MARK_REF_DATA_LAYER, DataLayerHome.getDataLayersReferenceList() );
        
        return getPage( PROPERTY_PAGE_TITLE_MANAGE_BASEMAPS, TEMPLATE_EXPORT_DATALAYER, model );
    }

    /**
     * Process the data layer export
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     * @throws AccessDeniedException
     */
    @Action( value = ACTION_EXPORT_DATALAYER, securityTokenDisabled = true )
    public String doExportDataLayer( HttpServletRequest request ) throws AccessDeniedException
    {
        int nDataLayer = Integer.parseInt( request.getParameter("layer_type") );
    	getLayerSolr( DataLayerHome.findByPrimaryKey( nDataLayer ).get( ) );

        return redirectView( request, VIEW_EXPORT_DATALAYER );
    }

    public void getLayerSolr( DataLayer datalayer )
    {
    	
    	SolrSearchEngine engine = SolrSearchEngine.getInstance( );

        List<String> lstGeoJSONCoordinates = new ArrayList<>( );
        
        //int nlimit = AppPropertiesService.getPropertyInt( PROPERTY_LIMIT_RESULT_SOLR, 100 );
        int nlimit = 100;

        List<SolrSearchResult> listResultsGeoloc = engine.getGeolocSearchResults( PARAMETER_SOLR_GEOJSON + ":" + datalayer.getSolrTag( ), null, nlimit );
        lstGeoJSONCoordinates = getGeolocModel( listResultsGeoloc, datalayer ) ;

        FileWriter fileWriter;
		try {
			fileWriter = new FileWriter( DIRECTORY_FILENAME + "/"+ "ExtractCartoLayer.json" );
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
			for ( int i = 0; i < lstGeoJSONCoordinates.size( ); i++ )
			{
				if ( i < lstGeoJSONCoordinates.size() - 1 )
					fileWriter.write( lstGeoJSONCoordinates.get( i ) + "," + System.lineSeparator( ) );
				else
					fileWriter.write( lstGeoJSONCoordinates.get( i ) + System.lineSeparator( ) );
	        }
			fileWriter.write( "] }" );
		    fileWriter.close( );
		        
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
		        
		    _temporaryFileGeneratorService.generateFile( new TemporaryFileExtract(), getUser( ) );		             
		} 
		catch ( IOException e ) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
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
                String strJson = (String) entry.getValue( );
                
                lstGeoJSONCoordinates.add( strJson );
            }       
        }
        return lstGeoJSONCoordinates;
    } 
}
