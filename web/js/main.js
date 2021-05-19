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

const lineStyle = new Style( {stroke: new Stroke( {color: '#9704b1', width: 3} )} );
const trajectoriesService = '/api/trajectories/search/bbox';

const format = new GeoJSON( {dataProjection: 'EPSG:4326', featureProjection: 'EPSG:3857'} );

//controls whether or not to load trajectories in the map.
let isTrajectoriesActive=true;

const trajectorySource = new VectorSource(
    {
        loader: function (extent, resolution, projection) {
            if ( !isTrajectoriesActive || extent[0] === -Infinity ) {
                return;
            }
            let url = buildFetchUrl( extent );
            dataFetch( url );
        },
        strategy: bbox
    } );



const trajectories = new VectorLayer( {
                                          source: trajectorySource,
                                          style: lineStyle
                                      } )

const OSMBackground = new Tile( {source: new OSM()} );

const Beijing = [116.38, 39.92];
const LosAngeles = [-118.24, 34.05];
var map = new Map( {
                       target: document.getElementById( 'map' ),
                       layers: [OSMBackground, trajectories],
                       view: new View( {center: olProj.fromLonLat( LosAngeles ), zoom: 9} )
                   } );

// Main behavior

const buildFetchUrl = function (extent) {
    let ll = olProj.toLonLat( [extent[0], extent[1]] );
    let ur = olProj.toLonLat( [extent[2], extent[3]] );
    let bbox = `${ll[0]},${ll[1]},${ur[0]},${ur[1]}`;
    return `${trajectoriesService}?bbox=${bbox}`;
};

/**
 * Fetch data using specified URL and load into the VectorSource
 * @param url
 */
const dataFetch = function (url) {
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
                   let features = trajectoryData.map(
                       tr => {
                           let f = new Feature( {
                                                    geometry: format.readGeometry( tr['geometry'] ),
                                                    start: tr['start'],
                                                    duration: tr['durationInMinutes']
                                                } );
                           f.setId( tr['_links']['self']['href'] );
                           return f;
                       }
                       );
                   trajectorySource.addFeatures( features );
               }
        ).catch( error => {
            console.log( "Error loading data", error );
            alert( "Error loading data. Try Reset." );
    } );
};

const formatFeatureAsRow = function (feature) {
    let url = feature.getId();
    let duration = feature.get( 'duration' );
    return `<tr><td>${duration} min.</td><td><a href="${url}" target="_blank">link</a></td></tr>`;
};

const displayFeatureInfo = function (pixel) {
    var features = [];
    map.forEachFeatureAtPixel( pixel, function (feature) {
        features.push( feature );
    } );
    if ( features.length > 0 ) {
        let rows = features.map( f => formatFeatureAsRow( f ) );
        document.getElementById( 'info' ).innerHTML =
            `<table><thead><th>Duration</th><th>Link</th></thead><tbody>${rows.join( " " )}</tbody></table>`;
        map.getTarget().style.cursor = 'pointer';
    }
    else {
        document.getElementById( 'info' ).innerHTML = '&nbsp;';
        map.getTarget().style.cursor = '';
    }
};


map.on( 'click', function (evt) {
    displayFeatureInfo( evt.pixel );
} );

let checkBox = document.getElementById("show-trajectories");
checkBox.onclick = ( ev => {
    isTrajectoriesActive=ev.target.checked;
    if (!isTrajectoriesActive) {
        trajectorySource.clear(true);
    } else {
        trajectorySource.refresh();
    }
} )