package fr.paris.lutece.plugins.modules.cartography.solr.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import fr.paris.lutece.plugins.search.solr.business.SolrSearchEngine;
import fr.paris.lutece.plugins.search.solr.business.SolrSearchResult;
import fr.paris.lutece.plugins.search.solr.indexer.SolrItem;
import fr.paris.lutece.portal.service.util.AppLogService;

public class CartographyService {
	
	public static final String MARK_POINTS = "points";
	public static final String MARK_POINTS_GEOJSON = "geojson";
	public static final String MARK_POINTS_ID = "id";
	public static final String MARK_POINTS_FIELDCODE = "code";
	public static final String MARK_POINTS_TYPE = "type";
	public static final String MARK_DATA_LAYER_TITLE = "data_layer_title";
    public static final String MARK_DATA_LAYER = "data_layer";
    public static final String MARK_LAYER_EDITABLE = "data_layer_editable";
    public static final String MARK_MAP = "mapLoaded";
    public static final String MARK_BASEMAP = "basemap";
    public static final String MARK_LAYER_PROPERTIES = "layer_properties";
    public static final String MARK_LAYER_TYPE = "layer_type";
    public static final String PARAMETER_SOLR_GEOJSON = "DataLayer_text";
	
	/**
     * Returns a model with points data from a geoloc search
     * 
     * @param listResultsGeoloc
     *            the result of a search
     * @return the model
     */
    public static List<HashMap<String, Object>> getGeolocModel( List<SolrSearchResult> listResultsGeoloc, DataLayer datalayer, DataLayerMapTemplate dataLayerMapTemplate )
    {
        List<HashMap<String, Object>> points = new ArrayList<>( listResultsGeoloc.size( ) );
        Map<String, String> iconKeysCache = new HashMap<>( );

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
                GeolocItem geolocItem = null;

                try
                {
                    geolocItem = GeolocItem.fromJSON( strJson );
                }
                catch( IOException e )
                {
                    AppLogService.error( "SolrSearchApp: error parsing geoloc JSON: " + strJson + ", exception " + e );
                }

                //if ( geolocItem != null && geolocItem.getTypegeometry( ).equals( GeolocItem.VALUE_GEOMETRY_TYPE ) )
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
                    if (datalayer != null)
                    	h.put( MARK_DATA_LAYER_TITLE, datalayer.getTitle( ) );
                    //h.put( MARK_DATA_LAYER, datalayer );
                    if (dataLayerMapTemplate != null)
                    {
                    	h.put( MARK_LAYER_PROPERTIES, dataLayerMapTemplate );
                    	DataLayerType dataLayerType = DataLayerTypeHome.findByPrimaryKey( dataLayerMapTemplate.getLayerType() ).get( );
                    	h.put( MARK_LAYER_TYPE, dataLayerType );
                    }
                    points.add( h );
                }
            }
        }
        return points;
    }
    
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
    
    public static GeolocItemPolygon getGeolocItemPolygon( String coordinate, String strTypeGeometry )
    {
    	String[] lstCoordPolygon = coordinate.split(";");
        
        GeolocItemPolygon geoPolygon = new GeolocItemPolygon();
        List<List<Double>> polygonLonLoat = new ArrayList<>( );
        
        for (String coordPolygonXY : lstCoordPolygon )
        {
        	String [] coordPolygonXY2 = coordPolygonXY.split( "," );
        	double polygonx = Double.valueOf( coordPolygonXY2[0] );
            double polygony = Double.valueOf( coordPolygonXY2[1] );
            polygonLonLoat.add( Arrays.asList( polygonx, polygony ) );
        }
        
        
        HashMap<String, Object> geometryPolygon = new HashMap<>( );
        geometryPolygon.put( GeolocItem.PATH_GEOMETRY_COORDINATES, polygonLonLoat );
        geoPolygon.setGeometry( geometryPolygon );
        geoPolygon.setTypegeometry( strTypeGeometry );
        
        return geoPolygon;
    }
    
    public static void loadMapAndPoints(MapTemplate map, Map<String, Object> model)
    {
    	SolrSearchEngine engine = SolrSearchEngine.getInstance( );

        List<HashMap<String, Object>> points = new ArrayList<HashMap<String, Object>> ( );
        List<DataLayer> lstDatalayer = DataLayerMapTemplateHome.getDataLayerListByMapTemplateId( map.getId( ) );
        Optional<DataLayer> dataLayerEditable  = DataLayerHome.findDataLayerFromMapId( map.getId( ), true, false, false );
        
        for (DataLayer datalayer : lstDatalayer)
        {
        	List<SolrSearchResult> listResultsGeoloc = engine.getGeolocSearchResults( PARAMETER_SOLR_GEOJSON + ":" + datalayer.getSolrTag( ), null, 100 );
        	Optional<DataLayerMapTemplate> dataLayerMapTemplate = DataLayerMapTemplateHome.findByIdMapKeyIdDataLayerKey( map.getId( ), datalayer.getId( ) );
        	points.addAll( CartographyService.getGeolocModel( listResultsGeoloc, datalayer, dataLayerMapTemplate.get( ) ) );
        }

        model.put( CartographyService.MARK_POINTS, points );
        model.put( CartographyService.MARK_MAP, map );
        model.put( MARK_BASEMAP, BasemapHome.findByPrimaryKey( Integer.valueOf( map.getMapBackground( ) ) ).get( ).getUrl( ) );
        if ( dataLayerEditable.isPresent( ) )
        {
        	model.put( CartographyService.MARK_LAYER_EDITABLE, dataLayerEditable.get( ) );
        }
    }

}
