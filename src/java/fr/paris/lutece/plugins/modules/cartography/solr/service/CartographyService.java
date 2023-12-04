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
package fr.paris.lutece.plugins.modules.cartography.solr.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import java.util.Map.Entry;

import fr.paris.lutece.plugins.carto.business.BasemapHome;
import fr.paris.lutece.plugins.carto.business.DataLayer;
import fr.paris.lutece.plugins.carto.business.DataLayerHome;
import fr.paris.lutece.plugins.carto.business.DataLayerMapTemplate;
import fr.paris.lutece.plugins.carto.business.DataLayerMapTemplateHome;
import fr.paris.lutece.plugins.carto.business.DataLayerType;
import fr.paris.lutece.plugins.carto.business.DataLayerTypeHome;
import fr.paris.lutece.plugins.carto.business.MapTemplate;
import fr.paris.lutece.plugins.leaflet.business.GeolocItem;
import fr.paris.lutece.plugins.leaflet.business.GeolocItemPolygon;
import fr.paris.lutece.plugins.leaflet.service.IconService;
import fr.paris.lutece.plugins.modules.cartography.solr.provider.CartoSolrMarkerProvider;
import fr.paris.lutece.plugins.search.solr.business.SolrSearchEngine;
import fr.paris.lutece.plugins.search.solr.business.SolrSearchResult;
import fr.paris.lutece.plugins.search.solr.indexer.SolrItem;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

public class CartographyService
{

    public static final String MARK_POINTS = "points";
    public static final String MARK_POINTS_GEOJSON = "geojson";
    public static final String MARK_POINTS_ID = "id";
    public static final String MARK_POINTS_FIELDCODE = "code";
    public static final String MARK_POINTS_TYPE = "type";
    public static final String MARK_DATA_LAYER_TITLE = "data_layer_title";
    public static final String MARK_DATA_LAYER_POPUP = "data_layer_popup";
    public static final String MARK_DATA_LAYER = "data_layer";
    public static final String MARK_LAYER_EDITABLE = "data_layer_editable";
    public static final String MARK_MAP = "mapLoaded";
    public static final String MARK_BASEMAP = "basemap";
    public static final String MARK_LAYER_PROPERTIES = "layer_properties";
    public static final String MARK_LAYER_TYPE = "layer_type";
    public static final String PARAMETER_SOLR_GEOJSON = "DataLayer_text";
    public static final String MARK_LIMIT_VERTEX = "limit_vertex";

    private static final String PROPERTY_LIMIT_VERTEX = "map.limit.vertex";
    private static final String PROPERTY_LIMIT_RESULT_SOLR = "map.limit.result.solr";

    /**
     * Returns a model with points data from a geoloc search
     * 
     * @param listResultsGeoloc
     *            the result of a search
     * @return the model
     */
    public static List<HashMap<String, Object>> getGeolocModel( List<SolrSearchResult> listResultsGeoloc, DataLayer datalayer,
            DataLayerMapTemplate dataLayerMapTemplate )
    {
        List<HashMap<String, Object>> points = new ArrayList<>( listResultsGeoloc.size( ) );
        Map<String, String> iconKeysCache = new HashMap<>( );

        for ( SolrSearchResult result : listResultsGeoloc )
        {
            Map<String, Object> dynamicFields = result.getDynamicFields( );

            String uid = result.getId( );

            for ( Entry<String, Object> entry : dynamicFields.entrySet( ) )
            {

                if ( !entry.getKey( ).endsWith( SolrItem.DYNAMIC_GEOJSON_FIELD_SUFFIX ) )
                {
                    continue;
                }
                HashMap<String, Object> h = new HashMap<>( );
                String strJson = (String) entry.getValue( );
                GeolocItem geolocItem = null;

                try
                {
                    geolocItem = GeolocItem.fromJSON( strJson );
                }
                catch( IOException e )
                {
                    AppLogService.error( "SolrSearchApp: error parsing geoloc JSON: " + strJson + ", exception " + e );
                }

                // if ( geolocItem != null && geolocItem.getTypegeometry( ).equals( GeolocItem.VALUE_GEOMETRY_TYPE ) )
                if ( geolocItem != null )
                {
                    String strType = result.getId( ).substring( result.getId( ).lastIndexOf( '_' ) + 1 );
                    String strIcon;

                    if ( iconKeysCache.containsKey( geolocItem.getIcon( ) ) )
                    {
                        strIcon = iconKeysCache.get( geolocItem.getIcon( ) );
                    }
                    else
                    {
                        strIcon = IconService.getIcon( strType, geolocItem.getIcon( ) );
                        iconKeysCache.put( geolocItem.getIcon( ), strIcon );
                    }

                    geolocItem.setIcon( strIcon );
                    h.put( MARK_POINTS_GEOJSON, geolocItem.toJSON( ) );
                    h.put( MARK_POINTS_ID, result.getId( ).substring( result.getId( ).indexOf( '_' ) + 1, result.getId( ).lastIndexOf( '_' ) ) );
                    h.put( MARK_POINTS_FIELDCODE, entry.getKey( ).substring( 0, entry.getKey( ).lastIndexOf( '_' ) ) );
                    h.put( MARK_POINTS_TYPE, strType );
                    if ( datalayer != null )
                    {
                        h.put( MARK_DATA_LAYER_TITLE, datalayer.getTitle( ) );
                        if ( datalayer.getPopupContent( ) != null && !datalayer.getPopupContent( ).isEmpty( ) )
                        {
                            String popupContent = replaceMarker( datalayer.getPopupContent( ), uid, datalayer.getSolrTag( ) );
                            popupContent = popupContent.replaceAll( "[\\r\\n]+", "" );
                            h.put( MARK_DATA_LAYER_POPUP, popupContent );
                        }
                        else
                        {
                            h.put( MARK_DATA_LAYER_POPUP, StringUtils.EMPTY );
                        }

                    }
                    // h.put( MARK_DATA_LAYER, datalayer );
                    if ( dataLayerMapTemplate != null )
                    {
                        h.put( MARK_LAYER_PROPERTIES, dataLayerMapTemplate );
                        DataLayerType dataLayerType = DataLayerTypeHome.findByPrimaryKey( dataLayerMapTemplate.getLayerType( ) ).get( );
                        h.put( MARK_LAYER_TYPE, dataLayerType );
                    }
                    points.add( h );
                }
            }
        }
        return points;
    }

    // public static String replaceTokens(String text, Map<String, String> replacements)
    public static String replaceMarker( String text, String uid, String solrTag )
    {
        Pattern pattern = Pattern.compile( "\\[(.+?)\\]" );
        Matcher matcher = pattern.matcher( text );
        StringBuffer buffer = new StringBuffer( );

        CartoSolrMarkerProvider cartoMarker = new CartoSolrMarkerProvider( );

        while ( matcher.find( ) )
        {
            Map<String, String> replacements = cartoMarker.valueMarker( matcher.group( 1 ), solrTag, uid );
            String replacement = replacements.get( matcher.group( 1 ) );
            if ( replacement != null )
            {
                // matcher.appendReplacement(buffer, replacement);
                // see comment
                matcher.appendReplacement( buffer, "" );
                buffer.append( replacement );
            }
            else
            {
                matcher.appendReplacement( buffer, "" );
                buffer.append( " " );
            }
        }
        matcher.appendTail( buffer );
        return buffer.toString( );
    }

    /**
     * get a geolocitem point from coordinate
     * 
     * @param x
     *            coordinate x
     * @param y
     *            coordinate y
     * @param adresse
     * @return
     */
    public static GeolocItem getGeolocItemPoint( Double x, Double y, String adresse )
    {

        GeolocItem geolocItem = new GeolocItem( );
        HashMap<String, Object> properties = new HashMap<>( );
        properties.put( GeolocItem.PATH_PROPERTIES_ADDRESS, adresse );

        HashMap<String, Object> geometry = new HashMap<>( );
        geometry.put( GeolocItem.PATH_GEOMETRY_COORDINATES, Arrays.asList( x, y ) );
        geometry.put( GeolocItem.PATH_GEOMETRY_TYPE, GeolocItem.VALUE_GEOMETRY_TYPE );
        geolocItem.setGeometry( geometry );
        geolocItem.setProperties( properties );

        return geolocItem;
    }

    /**
     * get a geolocitem polygon or polyline from coordinates
     * 
     * @param coordinate
     * @param strTypeGeometry
     *            Polygon or Polyline
     * @return
     */
    public static GeolocItemPolygon getGeolocItemPolygon( String coordinate, String strTypeGeometry )
    {
        String [ ] lstCoordPolygon = coordinate.split( ";" );

        GeolocItemPolygon geoPolygon = new GeolocItemPolygon( );
        List<List<Double>> polygonLonLoat = new ArrayList<>( );

        for ( String coordPolygonXY : lstCoordPolygon )
        {
            String [ ] coordPolygonXY2 = coordPolygonXY.split( "," );
            double polygonx = Double.valueOf( coordPolygonXY2 [0] );
            double polygony = Double.valueOf( coordPolygonXY2 [1] );
            polygonLonLoat.add( Arrays.asList( polygonx, polygony ) );
        }

        HashMap<String, Object> geometryPolygon = new HashMap<>( );
        geometryPolygon.put( GeolocItem.PATH_GEOMETRY_COORDINATES, polygonLonLoat );
        geoPolygon.setGeometry( geometryPolygon );
        geoPolygon.setTypegeometry( strTypeGeometry );

        return geoPolygon;
    }

    /**
     * load model carto into the map
     * 
     * @param map
     * @param model
     */
    public static void loadMapAndPoints( MapTemplate map, Map<String, Object> model )
    {
        SolrSearchEngine engine = SolrSearchEngine.getInstance( );

        List<HashMap<String, Object>> points = new ArrayList<HashMap<String, Object>>( );
        List<DataLayer> lstDatalayer = DataLayerMapTemplateHome.getDataLayerListByMapTemplateId( map.getId( ) );
        Optional<DataLayer> dataLayerEditable = DataLayerHome.findDataLayerFromMapId( map.getId( ), true, false, false );
        int nlimit = AppPropertiesService.getPropertyInt( PROPERTY_LIMIT_RESULT_SOLR, 100 );

        for ( DataLayer datalayer : lstDatalayer )
        {
            List<SolrSearchResult> listResultsGeoloc = engine.getGeolocSearchResults( PARAMETER_SOLR_GEOJSON + ":" + datalayer.getSolrTag( ), null, nlimit );
            Optional<DataLayerMapTemplate> dataLayerMapTemplate = DataLayerMapTemplateHome.findByIdMapKeyIdDataLayerKey( map.getId( ), datalayer.getId( ) );
            points.addAll( CartographyService.getGeolocModel( listResultsGeoloc, datalayer, dataLayerMapTemplate.get( ) ) );
        }

        model.put( CartographyService.MARK_POINTS, points );
        model.put( CartographyService.MARK_MAP, map );
        model.put( MARK_BASEMAP, BasemapHome.findByPrimaryKey( Integer.valueOf( map.getMapBackground( ) ) ).get( ).getUrl( ) );
        model.put( MARK_LIMIT_VERTEX, AppPropertiesService.getProperty( PROPERTY_LIMIT_VERTEX ) );
        if ( dataLayerEditable.isPresent( ) )
        {
            model.put( CartographyService.MARK_LAYER_EDITABLE, dataLayerEditable.get( ) );
        }
    }

}
