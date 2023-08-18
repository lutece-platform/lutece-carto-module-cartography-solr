package fr.paris.lutece.plugins.modules.cartography.solr.service;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import fr.paris.lutece.plugins.carto.business.BasemapHome;
import fr.paris.lutece.plugins.carto.business.MapTemplate;
import fr.paris.lutece.plugins.carto.business.MapTemplateHome;
import fr.paris.lutece.plugins.search.solr.service.ISolrSearchAppAddOn;

public class CartoSolrAppAddon implements ISolrSearchAppAddOn {

    private static final String MARK_BASEMAP = "basemap";
    private static final String MARK_MAP = "mapLoaded";

    
	
	@Override
	public void buildPageAddOn(Map<String, Object> model, HttpServletRequest request) {
   
        //Charger map par defaut.
		MapTemplate map = MapTemplateHome.findXpageFrontOffice( ).get( );
        
        model.put( MARK_MAP, map );
        model.put( MARK_BASEMAP, BasemapHome.findByPrimaryKey( Integer.valueOf( map.getMapBackground( ) ) ).get( ).getUrl( ) );
		
	}

}
