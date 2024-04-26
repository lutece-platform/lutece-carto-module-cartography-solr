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
package fr.paris.lutece.plugins.cartography.modules.solr.indexer;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.ContentHandler;

import fr.paris.lutece.plugins.carto.business.Coordonnee;
import fr.paris.lutece.plugins.carto.business.CoordonneeHome;
import fr.paris.lutece.plugins.search.solr.business.field.Field;
import fr.paris.lutece.plugins.search.solr.indexer.SolrIndexer;
import fr.paris.lutece.plugins.search.solr.indexer.SolrIndexerService;
import fr.paris.lutece.plugins.search.solr.indexer.SolrItem;
import fr.paris.lutece.plugins.search.solr.util.LuteceSolrException;
import fr.paris.lutece.plugins.search.solr.util.LuteceSolrRuntimeException;
import fr.paris.lutece.plugins.search.solr.util.SolrConstants;
import fr.paris.lutece.plugins.search.solr.util.TikaIndexerUtil;
import fr.paris.lutece.portal.business.page.Page;
import fr.paris.lutece.portal.business.page.PageHome;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.page.IPageService;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.util.url.UrlItem;

/**
 * The indexer service for Solr.
 *
 */
public class SolrCoordinateIndexer implements SolrIndexer
{
    public static final String RESSOURCE_COORDONNEES = "COORDONNEES_COORDONNEE";
    public static final String NAME = "SolrCoordonneeIndexer";
    private static final String PARAMETER_PAGE_ID = "page_id";
    private static final String DESCRIPTION = "Solr coordonnees Indexer";
    private static final String VERSION = "1.0.0";
    private static final String TYPE = "PAGE";
    private static final String CATEGORIE = "GeoJSon";
    private static final String PROPERTY_INDEXER_ENABLE = "solr.indexer.page.enable";
    private static final String BEAN_PAGE_SERVICE = "pageService";
    private static final String SHORT_NAME = "page";
    private static final List<String> LIST_RESSOURCES_NAME = new ArrayList<>( );
    private static final String PAGE_INDEXATION_ERROR = "[SolrCoordonneeIndexer] An error occured during the indexation of the page number ";

    /**
     * Creates a new SolrPageIndexer
     */
    public SolrCoordinateIndexer( )
    {
        LIST_RESSOURCES_NAME.add( RESSOURCE_COORDONNEES );
    }

    /**
     * {@inheritDoc}
     */
    public List<String> indexDocuments( )
    {
        List<Coordonnee> lstCoord = CoordonneeHome.getCoordonneesList( );
        List<String> lstErrors = new ArrayList<>( );
        List<SolrItem> lstSolrItems = new ArrayList<>( );

        for ( Coordonnee coord : lstCoord )
        {
            try
            {
                // Generates the item to index
                SolrItem item = getItem( coord, SolrIndexerService.getBaseUrl( ) );
                lstSolrItems.add( item );
            }
            catch( Exception e )
            {
                lstErrors.add( PAGE_INDEXATION_ERROR + coord.getId( ) + " : " + SolrIndexerService.buildErrorMessage( e ) );
                AppLogService.error( PAGE_INDEXATION_ERROR + coord.getId( ), e );
            }
        }

        try
        {
            SolrIndexerService.write( lstSolrItems );
        }
        catch( Exception e )
        {
            lstErrors.add( SolrIndexerService.buildErrorMessage( e ) );
            AppLogService.error( PAGE_INDEXATION_ERROR, e );
        }

        return lstErrors;
    }

    /**
     * Builds a document which will be used by Lucene during the indexing of the pages of the site with the following fields : summary, uid, url, contents,
     * title and description.
     *
     * @return the built Document
     * @param strUrl
     *            The base URL for documents
     * @param page
     *            the page to index
     * @throws SiteMessageException
     *             occurs when a site message need to be displayed
     */
    private SolrItem getItem( Coordonnee coord, String strUrl ) throws SiteMessageException
    {

        // make a new, empty SolrItem
        SolrItem solrItem = new SolrItem( );
        String nIdCoordinate = String.valueOf( coord.getId( ) );
        solrItem.setIdResource( nIdCoordinate );
        solrItem.setSite( SolrIndexerService.getWebAppName( ) );
        solrItem.setRole( "Cartography" );
        solrItem.setType( "Cartography" + "_" + coord.getId( ) );
        solrItem.setUid( coord.getId( ) + "_Coordonnees" );
        solrItem.setTitle( "Coordonnees" + " #" + nIdCoordinate );
        solrItem.setContent( "" );
        // solrItem.setDate( formResponse.getCreation( ) );
        solrItem.setUrl( "jsp/site/Portal.jsp?page=formsResponse&id_response=" + nIdCoordinate );

        solrItem.addDynamicFieldGeoloc( "coordonnee_geojson", coord.getGeoJson( ), "Coordonnee" );
        solrItem.addDynamicField( "DataLayer", String.valueOf( coord.getDataLayer( ).getSolrTag( ) ) );

        return solrItem;
    }

    /**
     * Returns the name of the indexer.
     *
     * @return the name of the indexer
     */
    @Override
    public String getName( )
    {
        return NAME;
    }

    /**
     * Returns the version.
     *
     * @return the version.
     */
    @Override
    public String getVersion( )
    {
        return VERSION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription( )
    {
        return DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnable( )
    {
        return "true".equalsIgnoreCase( AppPropertiesService.getProperty( PROPERTY_INDEXER_ENABLE ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Field> getAdditionalFields( )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SolrItem> getDocuments( String strIdDocument )
    {
        List<SolrItem> lstItems = new ArrayList<>( );
        try
        {
            int nIdDocument = Integer.parseInt( strIdDocument );
            // Page page = PageHome.getPage( nIdDocument );
            Coordonnee coord = CoordonneeHome.findByPrimaryKey( nIdDocument ).get( );
            lstItems.add( getItem( coord, SolrIndexerService.getBaseUrl( ) ) );
        }
        catch( Exception e )
        {
            throw new LuteceSolrRuntimeException( e.getMessage( ), e );
        }

        return lstItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getResourcesName( )
    {
        return LIST_RESSOURCES_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourceUid( String strResourceId, String strResourceType )
    {
        StringBuilder sb = new StringBuilder( strResourceId );
        sb.append( SolrConstants.CONSTANT_UNDERSCORE ).append( SHORT_NAME );

        return sb.toString( );
    }
}
