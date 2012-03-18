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
// MAPTYPE CONTROL
com.gmail.boiledorange73.map.MapTypeControl = function(mapTypes, cssFontSize, onChangeMap) {
  // event handler to cancel select text
  var cancelSelect = function(ev) {
      if( ev.preventDefault ) {
        ev.preventDefault();
      }
      return false;
    };
  // initliazes elements (exceptiong for buttons).
  this.elements = {
    "mt":
      com.gmail.boiledorange73.ut.createElement("div",{id: "mt"}),
    "mt-mount":
      com.gmail.boiledorange73.ut.createElement("div",{id: "mt-mount"}),
    "mt-current":
      com.gmail.boiledorange73.ut.createElement("div",{id: "mt-current"}),
    "mt-current-content":
      com.gmail.boiledorange73.ut.createElement("p",{id: "mt-current-content"})
  };
  this.setFontSize(cssFontSize);
  // temporarily appends to body
  var body = document.getElementsByTagName("body").item(0);
  body.appendChild(this.elements["mt"]);
  // builds tree
  this.elements["mt"].appendChild(this.elements["mt-mount"]);
  this.elements["mt-mount"].appendChild(this.elements["mt-current"]);
  this.elements["mt-current"].appendChild(this.elements["mt-current-content"]);
  // event
  var mt_current = this.elements["mt-current"];
  com.gmail.boiledorange73.ut.addEventHandler(mt_current, "click", function(_this){ return function(ev){ _this.toggleList();};}(this));
  com.gmail.boiledorange73.ut.addEventHandler(mt_current, "mousedown", cancelSelect);
  com.gmail.boiledorange73.ut.addEventHandler(mt_current, "selectstart", cancelSelect);

  // buttons
  this.buttons = {};
  var len = mapTypes != null ? mapTypes.length : 0;
  this.totalWidth = 0;
  var maxContentWidth = 0;
  for( var n = 0; n < len; n++ ) {
    var mt = mapTypes[n];
    // div (box) with special className and position to calculate maximum width
    var e = com.gmail.boiledorange73.ut.createElement("div",{className: "button selected", style: {position: "absolute"}});
    // p (content)
    var ec = document.createElement("p");
    com.gmail.boiledorange73.ut.addEventHandler(e, "mousedown", cancelSelect);
    com.gmail.boiledorange73.ut.addEventHandler(e, "selectstart", cancelSelect);
    ec.style.margin = "0";
    ec.style.padding = "0";
    ec.appendChild(document.createTextNode(mt.name));
    e.appendChild(ec);
    // appends
    this.elements["mt-mount"].appendChild(e);
    // width
    var cw = ec.clientWidth;
    ec.style.width = cw + "px";
    if( cw > maxContentWidth ) {
      maxContentWidth = cw;
    }
    var w = e.clientWidth;
    this.totalWidth += w;
    // pushes to array
    this.buttons[mt.id] = {"element": e, "name": mt.name, "width": w};
    // fixes width
    ec.style.width = cw + "px";
    // resets special style
    com.gmail.boiledorange73.ut.merge(e,{className: "button", style: {position: "static"}});
    // event
    com.gmail.boiledorange73.ut.addEventHandler(e, "click", function(_id){ return function(ev){onChangeMap(_id);}; }(mt.id));
  }
  // sets width for current content.
  this.elements["mt-current-content"].style.width = maxContentWidth + "px";

  // removes from body
  body.removeChild(this.elements["mt"]);
};

com.gmail.boiledorange73.map.MapTypeControl.prototype.resetState = function(mapTypeId) {
  // resets buttons
  for( var id in this.buttons ) {
    var one = this.buttons[id];
    if( id == mapTypeId ) {
      one.element.className = "button selected";
      com.gmail.boiledorange73.ut.setText(this.elements["mt-current-content"], one.name);
    }
    else {
      one.element.className = "button";
    }
  }
};

com.gmail.boiledorange73.map.MapTypeControl.prototype.resetPosition = function(mapWidth) {
  if( this.totalWidth + 100 > mapWidth ) {
    this.setLayout("collapse");
  }
  else {
    this.setLayout("flow");
  }
};

com.gmail.boiledorange73.map.MapTypeControl.prototype.toggleList = function() {
  var e = this.elements["mt-mount"];
  switch( e.className ) {
  case "select":
    this.setLayout("collapse");
    break;
  case "collapse":
    this.setLayout("select");
    break;
  }
};

com.gmail.boiledorange73.map.MapTypeControl.prototype.setLayout = function(type) {
  var className = null;

  switch( type ) {
  case "select":
    className = "select";
    break;
  case "collapse":
    className = "collapse";
    break;
  default:
    className = "flow";
  }
  this.elements["mt-mount"].className = className;
}

com.gmail.boiledorange73.map.MapTypeControl.prototype.setFontSize = function(cssFontSize) {
  this.elements["mt"].style.fontSize = cssFontSize;
};
