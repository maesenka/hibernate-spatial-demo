import OSM from 'ol/source/OSM';
import VectorSource from 'ol/source/Vector';
import VectorLayer from 'ol/layer/Vector';
import {Style, Stroke} from 'ol/style';
import GeoJSON from 'ol/format/GeoJSON';
import Feature from 'ol/Feature';
import Tile from 'ol/layer/Tile';
import View from 'ol/View';
import Map from 'ol/Map';
import * as olProj from 'ol/proj';
import {bbox} from 'ol/loadingstrategy';

let lineStyle = new Style( {stroke: new Stroke( {color: '#d85dee', width: 3} )} );
let trajectoriesService = '/api/trajectories/search/bbox';

let format = new GeoJSON( {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'});

let trajectorySource = new VectorSource(
    {
        loader: function (extent, resolution, projection) {
            if ( extent[0] === -Infinity ) {
                return;
            }
            let ll = olProj.toLonLat( [extent[0], extent[1]] );
            let ur = olProj.toLonLat( [extent[2], extent[3]] );
            let bbox = `${ll[0]},${ll[1]},${ur[0]},${ur[1]}`;
            let url = `${trajectoriesService}?bbox=${bbox}`;
            console.log( "Fetching URL:", url )
            fetch( url )
                .then( response => {
                    if ( response.ok ) {
                        return response.json();
                    }
                    else {
                        console.error( response );
                        return Promise.reject( response.status );
                    }
                } )
                .then( data => {
                           let trajectoryData = data['_embedded']['trajectories'];
                           console.log( "trajectoryData: ", trajectoryData.slice( 0, 10 ) );
                           let features = trajectoryData.map(
                               tr =>
                                   new Feature( {
                                                    geometry: format.readGeometry( tr['geometry'] ),
                                                    duration: tr['durationInMinutes']
                                                } ) );
                           console.log("features: ", features.slice(0, 10))
                           trajectorySource.addFeatures( features );
                       }
                );
        },
        strategy: bbox
    } );

var trajectories = new VectorLayer( {
                                        source: trajectorySource,
                                        style: lineStyle
                                    } )

var OSMBackground = new Tile( {source: new OSM()} );

var Beijing = [116.38, 39.92];
var LosAngeles = [-118.24, 34.05];

var map = new Map( {
                       target: document.getElementById( 'map' ),
                       layers: [
                           OSMBackground, trajectories
                       ],
                       view: new View( {center: olProj.fromLonLat( LosAngeles ), zoom: 9} )
                   } );

