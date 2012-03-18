//
// Main script.
//

// ----------------------------------------------------------------
// path
if( !window.com ) {
  window.com = {};
}
if( !com.gmail ) {
  com.gmail = {};
}
if( !com.gmail.boiledorange73 ) {
  com.gmail.boiledorange73 = {};
}
// ----------------------------------------------------------------
// js
if( !com.gmail.boiledorange73.js ) {
  com.gmail.boiledorange73.js = {};
}
// ----------------
// Send the message to Dalvik.
//   code - Code of the message. This looks like the name of the function.
//   message - Message body. This looks like the argument of the function.
// Note: code and message must be string. If other type, Dalvik recieves the string "undefined".
// ----------------
com.gmail.boiledorange73.js.sendMessage = function(code, message) {
  if( window.jsBridge ) {
    jsBridge.setMessage(code, message);
  }
};
// ----------------
// Queries to Dalvik.
// ----------------
com.gmail.boiledorange73.js.query = function(code, message) {
  if( window.jsBridge ) {
    return jsBridge.query(code, message);
  }
  return null;
}

// ----------------------------------------------------------------
// ut
com.gmail.boiledorange73.ut = {};

com.gmail.boiledorange73.ut.using = function(namespace) {
  $N = null;
  var ns = null;
  if( namespace != null ) {
    var arr = namespace.split(".");
    var len = arr != null ? arr.length : 0;
    if( len > 0 ) {
      ns = window;
      for( var n = 0; n < len; n++ ) {
        if( one[n] ) {
          ns = null;
          break;
        }
      }
    }
  }
  $N = ns;
};

com.gmail.boiledorange73.ut.createElement = function(tagName, attrs) {
  return com.gmail.boiledorange73.ut.merge(document.createElement(tagName), attrs);
};

com.gmail.boiledorange73.ut.merge = function(base, hash) {
  if( base == null ) {
    base = {};
  }
  if( hash != null ) {
    for( var k in hash ) {
      var b;
      var h = hash[k];
      if( typeof hash[k] == "object" ) {
        com.gmail.boiledorange73.ut.merge(base[k], hash[k]);
      }
      else {
        base[k] = hash[k];
      }
    }
  }
  return base;
};


com.gmail.boiledorange73.ut.addEventHandler = function(element, type, handler) {
  if( element.addEventListener) {
    element.addEventListener(type, handler, false);
  }
  else if( element.attachEvent ) {
    element.attachEvent('on'+type, handler);
  }
  else {
    element['on'+type.toLowerCase()] = function() { handler(); };
  }
};

com.gmail.boiledorange73.ut.deleteEventHandler = function(element, type, handler) {
  if( element.removeEventListener) {
    element.removeEventListener(type, handler, false);
  }
  else if( element.detachEvent ) {
    element.detachEvent('on'+type, handler);
  }
  else {
    element['on'+type] = null;
  }
};

com.gmail.boiledorange73.ut.setText = function(e, text) {
  e.innerHTML = "";
  e.appendChild(document.createTextNode(text));
};

// ----------------------------------------------------------------
// map
if( !com.gmail.boiledorange73.map ) {
  com.gmail.boiledorange73.map = {};
}

// ----------------------------------------------------------------
// WMS
// ----------------
// Constructor.
// name: Name of the layer.
// baseUrl: Base URL, like "http://www.foo.example/mapserv.cgi?"
// layers: List of layers joined with ",", like "Cntr,BldA,WL".
// maxZoom: Maximum value of the zoom. (Minimum zoom must be zero).
// alt: Alternative text for layer button on the map.
// ----------------
com.gmail.boiledorange73.map.WMS = function(name, baseUrl, layers, maxZoom, alt) {
  this.name = name;
  this.baseUrl = baseUrl;
  this.layers = layers;
  this.maxZoom = maxZoom;
  this.alt = alt;
};
// ----------------
// Returns tile size.
// ----------------
com.gmail.boiledorange73.map.WMS.prototype.tileSize = new google.maps.Size(256,256);
// ----------------
// Gets the tile as a DOM node.
// ----------------
com.gmail.boiledorange73.map.WMS.prototype.getTile = function(tile, zoom, ownerDocument) {
    var img = ownerDocument.createElement("img");
    img.style.width = this.tileSize.width + "px";
    img.style.height = this.tileSize.height + "px";
    img.src = this.baseUrl +
        "REQUEST=GetMap&" +
        "STYLES=&SRS=EPSG:900913&" +
        "BBOX=" + this.getBBox(tile,zoom) + "&" +
        "WIDTH=256&HEIGHT=256&FORMAT=image/png&" +
        "LAYERS=" + this.layers;
    return img;
  };
// ----------------
// Gets the Bounding Box text which expresses "minx,miny,maxx,maxy"(meters) for each tile.
// ----------------
com.gmail.boiledorange73.map.WMS.prototype.getBBox = function(tile,zoom) {
  var pxs = 256 * (1 << zoom);
  var pxsh = pxs / 2;
  var mtr = 40075017;
  var mpp = mtr/pxs;
  var x1 = (tile.x*256-pxsh) * mpp;
  var x2 = ((tile.x*256+256)-pxsh) * mpp;
  var y2 = (pxsh-tile.y*256) * mpp;
  var y1 = (pxsh-(tile.y*256+256)) * mpp;
  return x1 + "," + y1 + "," + x2 + "," + y2;
};
// ----------------------------------------------------------------
// TMS
// ----------------
// Constructor.
// name: Name of the layer.
// baseUrl: Base URL, like "http://www.foo.example/tms/". This must end with "/".
// alt: Alternative text for layer button on the map.
// extension: extension for each tile image. "png" by default.
// NOTE
//  Created URL is baseUrl + "{zoom}/{x}/{y}.{extension}". TMS version folder is ignored.
// ----------------
com.gmail.boiledorange73.map.TMS = function(name, baseUrl, maxZoom, alt, extension) {
  this.name = name;
  this.baseUrl = baseUrl;
  this.maxZoom = maxZoom;
  this.alt = alt;
  this.extension = extension == null ? "png" : extension;
};
// ----------------
// Returns tile size.
// ----------------
com.gmail.boiledorange73.map.TMS.prototype.tileSize = new google.maps.Size(256,256);
// ----------------
// Gets the tile as a DOM node.
// ----------------
com.gmail.boiledorange73.map.TMS.prototype.getTile = function(tile, zoom, ownerDocument) {
    var img = ownerDocument.createElement("img");
    img.style.width = this.tileSize.width + "px";
    img.style.height = this.tileSize.height + "px";
    var ty = (1<<zoom) - tile.y - 1;
    img.src = this.baseUrl + zoom + "/" + tile.x + "/" + ty + "." + this.extension;
    return img;
  };




// ----------------------------------------------------------------
// MAP
com.gmail.boiledorange73.map.LT_TMS = 0;

com.gmail.boiledorange73.map.Map = function(initialCenter, initialZoom, mapTypes, options) {
  // options
  var mapNameSize = null;
  if( options != null ) {
    mapNameSize = options["mapNameSize"];
  }
  // creates maptypeIds for map constructor.
  var mtids = [];
  for( var k in mapTypes ) {
    mtids.push(k);
  }
  mtids.push(google.maps.MapTypeId.ROADMAP);
  this.gmap = new google.maps.Map(
      document.getElementById("map"), {
        mapTypeControl: false,
        zoom: initialZoom,
        center: initialCenter,
        mapTypeControlOptions: {
          mapTypeIds: mtids
        }
      }
    );
  // registers maps.
  this.mapTypes = [];
  for( var k in mapTypes ) {
    var mt = new mapTypes[k]();
    this.mapTypes.push({"id":k, "name":mt.name});
    this.gmap.mapTypes.set(k, mt);
  }
  this.mapTypes.push({"id":google.maps.MapTypeId.ROADMAP, "name":mapviewer.rs.get("roadmap")});
  //
  this.mapTypeControl = new com.gmail.boiledorange73.map.MapTypeControl(this.mapTypes, mapNameSize, function(_this){return function(id){_this.restoreMapState(id,null,null,null);}}(this));
  // resize event
  google.maps.event.addListener(this.gmap, 'resize',
    function(_this) { return function(){ _this.mapTypeControl.resetPosition(document.getElementById("map").clientWidth);} }(this));
  // !! window.onresize !!
  var gmap = this.gmap;
  window.onresize = function() { google.maps.event.trigger(gmap, 'resize'); };
  google.maps.event.trigger(this.gmap, 'resize');
  // registers
  this.gmap.controls[google.maps.ControlPosition.TOP_RIGHT].push(this.mapTypeControl.elements["mt"]);
  // initializes map type
  this.restoreMapState(mtids[0], null, null, null);
};


// ----------------
// Sets size of map name text
// ----------------
com.gmail.boiledorange73.map.Map.prototype.setMapNameSize = function(cssFontSize) {
  this.mapTypeControl.setFontSize(cssFontSize);
};

// ----------------
// "Gets" (Send it to the Dalvik) array of map types whose element contains "id" and "name".
// ----------------
com.gmail.boiledorange73.map.Map.prototype.getMapTypesJson = function() {
  var arr = [];
  for( var n = 0; n < this.mapTypes.length; n++ ) {
    var mt = this.mapTypes[n];
    arr.push("  {\"id\":\""+mt.id+"\",\"name\":\""+mt.name+"\"}");
  }
  return "[\n" + arr.join(",\n") + "\n]\n";
}

// ----------------
// "Gets" (Send it to the Dalvik) the current status of the map.
// ----------------
com.gmail.boiledorange73.map.Map.prototype.getCurrentMapState = function() {
  if( this.gmap ) {
    var latlng = this.gmap.getCenter();
    var z = this.gmap.getZoom();
    var id = this.gmap.getMapTypeId();
    if( latlng ) {
      var lon = latlng.lng();
      var lat = latlng.lat();
      com.gmail.boiledorange73.js.sendMessage("getCurrentMapState", "{lon:"+lon+", lat:"+lat+", z:"+z+", id: \"" + id + "\"}") ;
    }
    com.gmail.boiledorange73.js.sendMessage("getCurrentMapState", "") ;
  }
};
// ----------------
// Sets the status of the map.
//   id - ID of active map. Affects only if id is NON-NULL value.
//   lon - Longitude. Affects only if lat and lon are NON-NULL values.
//   lat - Latitude. Affects only if lat and lon are NON-NULL values.
//   z - Zoom level. Affects only if z >= 0.
// ----------------
com.gmail.boiledorange73.map.Map.prototype.restoreMapState = function(id, lon, lat, z) {
  if( id ) {
    this.gmap.setMapTypeId(id);
    this.mapTypeControl.resetState(id);
  }
  if( lat != null && lon != null ) {
    this.gmap.setCenter( new google.maps.LatLng(lat, lon) );
  }
  if( z != null && z >= 0 ) {
    this.gmap.setZoom( z );
  }
}

// ----------------
// Marker for "My Location".
// ----------------
com.gmail.boiledorange73.map.Map.prototype.myLocation = {
    "latlng": null,
    "acc": null,
    "icon": new google.maps.MarkerImage(
        'ball-16x16.png',
        new google.maps.Size(16, 16),
        new google.maps.Point(0, 0),
        new google.maps.Point(7, 7)
      ),
    "marker" : null,
    "intervalId" : null
  };
// ----------------
// Sets "My Location". This is called by Dalvik.
//   lon - Longitude.
//   lat - Latitude.
//   acc - Accuracy (meters). Now, this is not used.
//   pri - Location priority code.
//        If this method is called with LOWER priority value before (i.e. before: 1 and this time: 5),
//        marker is NEVER reset at this time.
//        If this is NOT more than 1, the execution at this time is performed.
//   centering - Whether the map moves to the location.
//   availabletime - How long the marker is available.
// Dalvik->JS
// javascript:mapviewer.map.setMyLocation(135,35,0,10,true,5000)
// pri: 10 - onetime
// ----------------
com.gmail.boiledorange73.map.Map.prototype.setMyLocation = function(lon, lat, acc, pri, centering, availabletime) {
  if( !this.myLocation || !(this.myLocation.pri > 0) || pri <= this.myLocation.pri ) {
    this.clearMyLocation();
    this.myLocation.latlng = new google.maps.LatLng(lat, lon);
    this.myLocation.acc = acc;
    this.myLocation.marker = new google.maps.Marker({
        position: this.myLocation.latlng,
        icon: this.myLocation.icon,
        clickable: false,
        map: this.gmap
      });
    this.myLocation.pri = pri;
    this.myLocation.disappearAt = (new Date()).getTime() + availabletime;
    if( centering ) {
      this.gmap.setCenter(this.myLocation.latlng);
    }
    var objthis = this;
    this.intervalId = setInterval(function(){objthis.timeoutMyLocation();},500);
  }
};
// ----------------
// Called by Dalvik, when Dalvik cannot get the location.
// ----------------
com.gmail.boiledorange73.map.Map.prototype.timeoutMyLocation = function() {
  if( !this.myLocation || !(this.myLocation.disappearAt >= 0) ) {
    // already unefective
    this.clearMyLocation();
  }
  else {
    var current = (new Date()).getTime();
    if( current >= this.myLocation.disappearAt ) {
      this.clearMyLocation();
    }
  }
}

// ----------------
// Clears "My Location" marker.
// ----------------
com.gmail.boiledorange73.map.Map.prototype.clearMyLocation = function() {
  if( this.myLocation ) {
    if( this.myLocation.marker ) {
      this.myLocation.marker.setMap(null);
      this.myLocation.marker = null;
    }
    this.myLocation.disappearAt = -1;
    if( this.myLocation.intervalId != null ) {
      clearInterval(this.myLocation.intervalId);
      this.myLocation.intervalId = null;
    }
  }
};

com.gmail.boiledorange73.map.MapTypeButton = function(id, name) {
  this.id = id;
  this.name = name;
  this.element = document.createElement("div");
  this.element.style.background = "rgb(255,255,255)";
  this.element.style.border = "solid 1px #999";
  this.element.style.padding = "0.25em 1em 0.25em 1em";
  this.element.whiteSpaec = "pre";
  this.appendChild(document.createTextNode(name));
};

com.gmail.boiledorange73.map.MapTypeButton.prototype.setStatus = function(isList) {
  if( isList ) {
    this.element.style.position = "static";
    this.element.style.display = "inline";
  }
  else {
    this.element.style.position = "relative";
    this.element.style.display = "block";
  }
};


// ----------------------------------------------------------------
// Application specific

if( !window.mapviewer ) {
  window.mapviewer = {};
}

mapviewer.map = null;

// ----------------
// Initialization
// ----------------
mapviewer.init = function() {
  // Searches for "ext" parameter value.
  var ext = null, lc = null, mnt;

  var property = com.gmail.boiledorange73.js.query("getProperty", null);

  ext = com.gmail.boiledorange73.js.query("getExtension",null);
  if( ext == null ) {
    ext = "jpg";
  }
  lc = com.gmail.boiledorange73.js.query("getLC",null);
  if( lc != null && lc != "ja" ) {
    mapviewer.rs.lang = lc;
  }
  mns = com.gmail.boiledorange73.js.query("getMapNameSize",null);

  // ---------------- creates maptypes
  // tokyo 5000
  var mapTypes= {};
  mapTypes.Tokyo5000 = function() {}
//  mapTypes.Tokyo5000.prototype = new com.gmail.boiledorange73.map.WMS(mapviewer.rs.get("tokyo5000"), "http://www.finds.jp/ws/hagwc.cgi?VERSION=1.1.1&", "ha:Tokyo5000", 17, mapviewer.rs.get("tokyo5000alt"));
  mapTypes.Tokyo5000.prototype = new com.gmail.boiledorange73.map.TMS(mapviewer.rs.get("tokyo5000"), "http://www.finds.jp/ws/tms/1.0.0/Tokyo5000-900913/", 18, mapviewer.rs.get("tokyo5000alt"), ext);

  // kanto rapid
  mapTypes.RapidKanto = function() {}
//  mapTypes.RapidKanto.prototype = new com.gmail.boiledorange73.map.WMS(mapviewer.rs.get("rapidkanto"), "http://www.finds.jp/ws/hagwc.cgi?VERSION=1.1.1&", "ha:Kanto_Rapid", 16, mapviewer.rs.get("rapidkantoalt"));
  mapTypes.RapidKanto.prototype = new com.gmail.boiledorange73.map.TMS(mapviewer.rs.get("rapidkanto"), "http://www.finds.jp/ws/tms/1.0.0/Kanto_Rapid-900913/", 17, mapviewer.rs.get("rapidkantoalt"), ext);

  // ---------------- initialization
  // creates maptypeIds for map constructor.
  mapviewer.map = new com.gmail.boiledorange73.map.Map(new google.maps.LatLng(35.68721, 139.7704), 15, mapTypes, {mapNameSize: mns});
  // -------- Sends the message that JS is loaded to Dalvik.
  // DO NOT DELETE BELOW !!
  com.gmail.boiledorange73.js.sendMessage("onLoad",mapviewer.map.getMapTypesJson());
};


//
window.onload = mapviewer.init;
