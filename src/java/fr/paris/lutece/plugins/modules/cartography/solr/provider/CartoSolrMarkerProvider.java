package fr.paris.lutece.plugins.modules.cartography.solr.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.solr.client.solrj.response.FacetField;

import fr.paris.lutece.plugins.carto.provider.IMarkerProvider;
import fr.paris.lutece.plugins.carto.provider.InfoMarker;
import fr.paris.lutece.plugins.modules.cartography.solr.service.CartographyService;
import fr.paris.lutece.plugins.search.solr.business.SolrFacetedResult;
import fr.paris.lutece.plugins.search.solr.business.SolrSearchEngine;
import fr.paris.lutece.plugins.search.solr.business.SolrSearchResult;
import fr.paris.lutece.plugins.search.solr.business.field.Field;
import fr.paris.lutece.plugins.search.solr.business.field.SolrFieldManager;
import fr.paris.lutece.plugins.search.solr.indexer.SolrItem;
import fr.paris.lutece.plugins.search.solr.service.SolrSearchAppConfService;
import fr.paris.lutece.portal.service.search.SearchResult;

public class CartoSolrMarkerProvider implements IMarkerProvider {

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return "1";
	}

	@Override
	public String getTitleI18nKey() {
		// TODO Auto-generated method stub
		return "Signets SOLR";
	}

	@Override
	public Collection<InfoMarker> provideMarkerDescriptions( String solrTag, HttpServletRequest request ) {
		
		List<InfoMarker> listMarkers = new ArrayList<>( );
		
		SolrSearchEngine engine = SolrSearchEngine.getInstance( );
		
		List<SearchResult> searchResults = engine.getSearchResults(CartographyService.PARAMETER_SOLR_GEOJSON + ":" + solrTag, request);
		//SolrFacetedResult facetedResult = engine.getFacetedSearchResults( CartographyService.PARAMETER_SOLR_GEOJSON + ":" + solrTag, new String [] {SolrSearchAppConfService.loadConfiguration( null ).getFieldList()}, null, "uid","asc", 100, 1 ,
		SolrFacetedResult facetedResult = engine.getFacetedSearchResults( CartographyService.PARAMETER_SOLR_GEOJSON + ":" + solrTag, new String [] { CartographyService.PARAMETER_SOLR_GEOJSON }, "uid","asc", 10, 1 ,
                100, false );
        List<SolrSearchResult> listResults = facetedResult.getSolrSearchResults( );
        List<FacetField> lstfield = facetedResult.getFacetFields();
        
        //List<FacetField> lstMarkerField = new ArrayList<>( );
        for ( FacetField facet : lstfield )
        {
        	InfoMarker notifyGruMarkerEntry = new InfoMarker( facet.getName( ) );
	        notifyGruMarkerEntry.setDescription( facet.getName( ) );
	        listMarkers.add( notifyGruMarkerEntry );
        }
        
        
        for (SolrSearchResult result : listResults )
		{
        	result.getDynamicFields( );
        	
        	Map<String, Object> dynamicFields = result.getDynamicFields( );

            for ( Entry<String, Object> entry : dynamicFields.entrySet( ) )
            {
            	if ( entry.getKey( ).endsWith( SolrItem.DYNAMIC_TEXT_FIELD_SUFFIX ) )
            	{           
	                
	    	        boolean isMarkerPresent = false;
	    	        for ( InfoMarker marker : listMarkers )
	    	        {
	    	        	if ( marker.getDescription( ).equals( entry.getKey( ) ) )
	    	        		isMarkerPresent = true;
	    	        }
	    	        if ( !isMarkerPresent )
	    	        {
	    	        	InfoMarker markerEntry = new InfoMarker( entry.getKey( ) );
		    	        markerEntry.setDescription( entry.getKey( ) );
	    	        	listMarkers.add( markerEntry );
	    	        }
            	}

            }
        }
		
        return listMarkers;
	}

	@Override
	public Collection<InfoMarker> provideMarkerValues( String solrTag, String uid )
	{
		List<InfoMarker> listMarkers = new ArrayList<>( );

        SolrSearchEngine engine = SolrSearchEngine.getInstance( );
		
		SolrFacetedResult facetedResult = engine.getFacetedSearchResults( CartographyService.PARAMETER_SOLR_GEOJSON + ":" + solrTag + "+AND+uid:" + uid, new String [] {SolrSearchAppConfService.loadConfiguration( null ).getFieldList()}, null, "uid","asc", 100, 1 ,
                100, false );
        List<SolrSearchResult> listResults = facetedResult.getSolrSearchResults( );
        List<FacetField> lstfield = facetedResult.getFacetFields();
        
        Map<String, Field> mapFields = SolrFieldManager.getFacetList( );
        mapFields.get("uid");
        
        for ( FacetField facet : lstfield )
        {
        	InfoMarker notifyGruMarkerEntry = new InfoMarker( facet.getName( ) );
	        notifyGruMarkerEntry.setValue( facet.getValues( ).get(0).getName( ) );
	        listMarkers.add( notifyGruMarkerEntry );
        }

        return listMarkers;
	}

	@Override
	public Map<String, String> valueMarker(String marker, String solrTag, String uid) {
		Map<String, String> mapFacet = new HashMap<>( );
		
		List<InfoMarker> listMarkers = new ArrayList<>( );

        SolrSearchEngine engine = SolrSearchEngine.getInstance( );
		
		SolrFacetedResult facetedResult = engine.getFacetedSearchResults( "uid:" + uid, new String [] {SolrSearchAppConfService.loadConfiguration( null ).getFieldList()}, null, "uid","asc", 100, 1 ,
                100, false );
        List<SolrSearchResult> listResults = facetedResult.getSolrSearchResults( );
        List<FacetField> lstfield = facetedResult.getFacetFields();
        
        Map<String, Field> mapFields = SolrFieldManager.getFacetList( );
        mapFields.get("uid");
        
        for (SolrSearchResult result : listResults )
		{
			result.getTitle( );
			
			Map<String, Object> dynamicFields = result.getDynamicFields( );

            for ( Entry<String, Object> entry : dynamicFields.entrySet( ) )
            {
                
            	if ( entry.getKey( ).endsWith( SolrItem.DYNAMIC_TEXT_FIELD_SUFFIX ) )
                {
            		mapFacet.put( entry.getKey( ), (String) entry.getValue( ) );
                }
            }
			
		}
        
        return mapFacet;
	}

}
