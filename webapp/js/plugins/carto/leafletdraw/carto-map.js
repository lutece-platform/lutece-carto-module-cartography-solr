var loadresource, map;
loadresource = document.createElement('link');
loadresource.setAttribute("rel", "stylesheet");
loadresource.setAttribute("type", "text/css");
loadresource.setAttribute("href", "js/plugins/leaflet/leaflet/leaflet.css");
document.getElementsByTagName("head")[0].appendChild(loadresource);
loadresource = document.createElement('link');
loadresource.setAttribute("rel", "stylesheet");
loadresource.setAttribute("type", "text/css");
loadresource.setAttribute("href", "js/plugins/leaflet/leaflet/MarkerCluster.css");
document.getElementsByTagName("head")[0].appendChild(loadresource);
loadresource = document.createElement('link');
loadresource.setAttribute("rel", "stylesheet");
loadresource.setAttribute("type", "text/css");
loadresource.setAttribute("href", "js/plugins/leaflet/leaflet/MarkerCluster.Default.css");
document.getElementsByTagName("head")[0].appendChild(loadresource);

loadresource = document.createElement('link');
loadresource.setAttribute("rel", "stylesheet");
loadresource.setAttribute("type", "text/css");
loadresource.setAttribute("href", "js/admin/plugins/carto/leaflet.draw.css");
document.getElementsByTagName("head")[0].appendChild(loadresource);

loadresource = document.createElement('script');
loadresource.setAttribute("type", "text/javascript");
loadresource.setAttribute("src", "js/plugins/leaflet/leaflet/leaflet.js" );
loadresource.async = false;
document.getElementsByTagName("head")[0].appendChild(loadresource);
loadresource = document.createElement('script');
loadresource.setAttribute("type", "text/javascript");
loadresource.setAttribute("src", "js/plugins/leaflet/leaflet/leaflet.markercluster.js" );
loadresource.async = false;
document.getElementsByTagName("head")[0].appendChild(loadresource);
loadresource = document.createElement('script');
loadresource.setAttribute("type", "text/javascript");
loadresource.setAttribute("src", "js/admin/plugins/carto/leaflet.draw.js" );
loadresource.async = false;
document.getElementsByTagName("head")[0].appendChild(loadresource);

loadresource = document.createElement('script');
loadresource.setAttribute("type", "text/javascript");
loadresource.setAttribute("src", "js/admin/plugins/carto/BoundaryCanvas.js" );
loadresource.async = false;
document.getElementsByTagName("head")[0].appendChild(loadresource);

$(window).on('load', function(){
    map = L.map('forms-admin-map').setView([48.85632, 2.33272], 12);
    //var points = JSON.parse($("#geojson_points").text());
    // create the tile layer with correct attribution
    var osmUrl='http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
    var osmAttrib='Map data © <a href="http://openstreetmap.org">OpenStreetMap</a> contributors';
    var osm = new L.TileLayer(osmUrl, {minZoom: 8, maxZoom: 16, attribution: osmAttrib}).addTo(map);
    //var markers = L.markerClusterGroup().addTo(map);
    
    
    var geom = {"type":"Polygon","coordinates":[
        [[48.837, 2.3], [48.85, 2.34], [48.9, 2.35] ]
    ]};
	
	
	var points = [
            <#list points as point>
            {
                "type": "${point.type}",
                "code": "${point.code}",
                "id": "${point.id}",
                "geojson": ${point.geojson}
            }<#if point_has_next>,</#if>
            </#list>
        ];
	
	
	
	for (var i = 0; i < points.length; i++) {
            var markers = undefined;
            var icon = undefined;
            var layer = "";
            var popupContent = undefined;

            var properties = points[i]["geojson"]["properties"];
            if (typeof(properties) != 'undefined') {
                layer = properties["layer"];
            }
            if (!layer) {
                layer = points[i]["code"];
            }
            if (typeof(marker_clusters[layer]) != "undefined") {
                markers = marker_clusters[layer];
                icon    = marker_icons[layer];
            } else {
                if (typeof(properties) != 'undefined' && typeof(properties["icon"]) != 'undefined') {
                    var clusterIconCreateFunction = undefined;
                    if (properties["icon"] == 'red' ) {
                        icon = redIcon;
                        clusterIconCreateFunction = createRedCluster;
                    } else if (properties["icon"] == 'green' ) {
                        icon = greenIcon;
                        clusterIconCreateFunction = createGreenCluster;
                    } else if (properties["icon"] == 'yellow' ) {
                        icon = yellowIcon;
                        clusterIconCreateFunction = createYellowCluster;
                    }
                    if (typeof(clusterIconCreateFunction) != 'undefined') {
                        markers = new L.MarkerClusterGroup({
                            iconCreateFunction: clusterIconCreateFunction
                        });
                    }
                }
                if (typeof(icon) == 'undefined') {
                    markers = new L.MarkerClusterGroup();
                    icon = new L.Icon.Default();
                }
                marker_clusters[layer] = markers;
                marker_icons[layer] = icon;
            }
            var marker = L.marker([points[i]["geojson"]["geometry"]["coordinates"][1],points[i]["geojson"]["geometry"]["coordinates"][0]] ,{icon: icon});

            if ( (typeof(properties) != 'undefined') && (typeof(properties["popupContent"]) != 'undefined') ) {
                if (properties["popupContent"]) {
                    marker.bindPopup(properties["popupContent"])
                }
            } else {
                popupContent = "<p>Loading " + points[i]["type"] + " " + points[i]["id"] + " " + points[i]["code"] + "...</p>";
                marker.bindPopup(popupContent)
                marker.on('click', (function(point) {
                    return function(e) {
                        var properties = point["properties"];
                        var popup = e.target.getPopup();
                        var url;
                        if ( (typeof(properties) != 'undefined') && (typeof(properties["popupAjax"]) != 'undefined') ) {
                            url = properties["popupAjax"];
                        } else {
                            url = "rest/leaflet/popup/" + point["type"] + "/" + point["id"] + "/" + point["code"];
                        }
     
                        $.get(url).done(function(data) {
                            popup.setContent(data);
                            popup.update();
                        }).fail(function() {
                            map.closePopup();
                        });
                    };
                })(points[i]));
            }

            markers.addLayer(marker);
        }
	
	
	
	var marker = L.marker([51.5, -0.09]).addTo(map);
    var marker2 = L.marker([48.85632, 2.33272]).addTo(map);
    

                var popup = L.popup();

 

                var marker = L.marker([48.85, 2.40]).addTo(map)

                               .bindPopup('<b>Popup</b><br />.').openPopup();

 

 

                

 

                var polygon = L.polygon([

                               [48.88, 2.4],

                               [48.93, 2.45],

                               [48.9, 2.48]

                ], {

                               color: 'black',

                               fillColor: 'yellow',

                               fillOpacity: 1                     

                }).addTo(map).bindPopup('Ici');
    
    
    

    
    /*
    var features= points.features;
    for (var i = 0; i < features.length; i++) {
        var coordinates = features[i]["geometry"]["coordinates"];
        var marker = L.marker([coordinates[1],coordinates[0]]);
        var properties = features[i]["properties"];
        marker.bindPopup(properties["popupContent"])
        markers.addLayer(marker);
    }
    */
    
    //map.on('click', addMarker);
    
    // Initialise the FeatureGroup to store editable layers
	var editableLayers = new L.FeatureGroup();
	map.addLayer(editableLayers);
	
	var drawPluginOptions = {
	  position: 'topright',
	  draw: {
	    polygon: {
	      allowIntersection: false, // Restricts shapes to simple polygons
	      drawError: {
	        color: '#e1e100', // Color the shape will turn when intersects
	        message: '<strong>Oh snap!<strong> you can\'t draw that!' // Message that will show when intersect
	      },
	      shapeOptions: {
	        color: '#97009c'
	      }
	    },
	    // disable toolbar item by setting it to false
	    polyline: true,
	    circle: false, // Turns off this drawing tool
	    rectangle: false,
	    marker: true,
	    },
	  edit: {
	    featureGroup: editableLayers, //REQUIRED!!
	    remove: false
	  }
	};
	
	// Initialise the draw control and pass it the FeatureGroup of editable layers
	var drawControl = new L.Control.Draw(drawPluginOptions);
	map.addControl(drawControl);
	
	var editableLayers = new L.FeatureGroup();
	map.addLayer(editableLayers);
	
	map.on('draw:created', function(e) {
	  var type = e.layerType,
	    layer = e.layer;
	
	  if (type === 'marker') {
	    layer.bindPopup('A popup!');
	  }
	
	  editableLayers.addLayer(layer);
	});
	
	// Truncate value based on number of decimals
        var _round = function(num, len) {
            return Math.round(num*(Math.pow(10, len)))/(Math.pow(10, len));
        };
        // Helper method to format LatLng object (x.xxxxxx, y.yyyyyy)
        var strLatLng = function(latlng) {
            return "("+_round(latlng.lat, 6)+", "+_round(latlng.lng, 6)+")";
        };

        // Generate popup content based on layer type
        // - Returns HTML string, or null if unknown object
        var getPopupContent = function(layer) {
            // Marker - add lat/long
            if (layer instanceof L.Marker || layer instanceof L.CircleMarker) {
                return strLatLng(layer.getLatLng());
            // Circle - lat/long, radius
            } else if (layer instanceof L.Circle) {
                var center = layer.getLatLng(),
                    radius = layer.getRadius();
                return "Center: "+strLatLng(center)+"<br />"
                      +"Radius: "+_round(radius, 2)+" m";
            // Rectangle/Polygon - area
            } else if (layer instanceof L.Polygon) {
                var latlngs = layer._defaultShape ? layer._defaultShape() : layer.getLatLngs(),
                    area = L.GeometryUtil.geodesicArea(latlngs);
                return "Area: "+L.GeometryUtil.readableArea(area, true);
            // Polyline - distance
            } else if (layer instanceof L.Polyline) {
                var latlngs = layer._defaultShape ? layer._defaultShape() : layer.getLatLngs(),
                    distance = 0;
                if (latlngs.length < 2) {
                    return "Distance: N/A";
                } else {
                    for (var i = 0; i < latlngs.length-1; i++) {
                        distance += latlngs[i].distanceTo(latlngs[i+1]);
                    }
                    return "Distance: "+_round(distance, 2)+" m";
                }
            }
            return null;
        };

        // Object created - bind popup to layer, add to feature group
        map.on(L.Draw.Event.CREATED, function(event) {
            var layer = event.layer;
            var content = getPopupContent(layer);
            if (content !== null) {
                layer.bindPopup(content);
            }
            //drawnItems.addLayer(layer);
        });

        // Object(s) edited - update popups
        map.on(L.Draw.Event.EDITED, function(event) {
            var layers = event.layers,
                content = null;
            layers.eachLayer(function(layer) {
                content = getPopupContent(layer);
                if (content !== null) {
                    layer.setPopupContent(content);
                }
            });
        });
        
        function isMarkerInsidePolygon(marker, poly) {
	    var polyPoints = poly.getLatLngs();       
	    var x = marker.getLatLng().lat, y = marker.getLatLng().lng;

	    var inside = false;
	    for (var i = 0, j = polyPoints.length - 1; i < polyPoints.length; j = i++) {
		var xi = polyPoints[i].lat, yi = polyPoints[i].lng;
		var xj = polyPoints[j].lat, yj = polyPoints[j].lng;

		var intersect = ((yi > y) != (yj > y))
		    && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
		if (intersect) inside = !inside;
	    }

	    return inside;
	};
        
        // Empêcher l'utilisateur de placer des marqueurs en dehors du polygone
	//L.Draw.Marker.include(polygon1);
	
	var polygon1 = L.polygon([

                               [48.837, 2.3],

                               [48.85, 2.34],

                               [48.9, 2.35]

                ], {

                               color: 'black',

                               fillColor: 'red',

                               fillOpacity: 0                     

                }).addTo(map).bindPopup('Zone d\'exclusion');
	
	L.Draw.Marker.include({
	  _onClick: function () {
	    // our hack
	    if (! isMarkerInsidePolygon(this._marker, polygon1)) {
	    return;
	    }
	    // original code
	    this._fireCreatedEvent();
	    this.disable();
	    if (this.options.repeatMode) {
	      this.enable();
	    }
	  }
	});
	
	L.Draw.Polyline.include({
	  _createMarker: function (latlng) {
		var marker = new L.Marker(latlng, {
			icon: this.options.icon,
			zIndexOffset: this.options.zIndexOffset * 2
		});

		if (! isMarkerInsidePolygon(marker, polygon1)) {
	    return;
	    }

		this._markerGroup.addLayer(marker);

		return marker;
	},
	addVertex: function (latlng) {
		var markersLength = this._markers.length;
		// markersLength must be greater than or equal to 2 before intersections can occur
		if (markersLength >= 2 && !this.options.allowIntersection && this._poly.newLatLngIntersects(latlng)) {
			this._showErrorTooltip();
			return;
		}
		else if (this._errorShown) {
			this._hideErrorTooltip();
		}
		
		//NLG
		var marker = new L.Marker(latlng, {
			icon: this.options.icon,
			zIndexOffset: this.options.zIndexOffset * 2
		});

		if (! isMarkerInsidePolygon(marker, polygon1)) {
		    return;
		    }
		
		
		this._markers.push(this._createMarker(latlng));

		this._poly.addLatLng(latlng);

		if (this._poly.getLatLngs().length === 2) {
			this._map.addLayer(this._poly);
		}

		this._vertexChanged(latlng, true);
	}
	});
	
});

function addMarker(e){
    // Add marker to map at click location; add popup window
    var newMarker = new L.marker(e.latlng).addTo(map);
}

/* Add local storage param to show/hide map 
$( function(){
    const map = '#forms-admin-map', btnMap = '#forms-admin-map + button';
    let mapVisibity = localStorage.getItem( 'admin-forms-map-visibility' );
    let isMapVisible = mapVisibity != null ? ( mapVisibity === 'true' ) : true;
    if( !isMapVisible ){
        $( map ).toggle();
        $( btnMap ).children().toggleClass('fa-expand');
    }
    $( '#admin-forms-map-toggle' ).on( 'click', function(){
        $( map ).toggle();
        $( btnMap ).children().toggleClass('fa-expand');
        localStorage.setItem( 'admin-forms-map-visibility' , !isMapVisible )
    });
})
*/