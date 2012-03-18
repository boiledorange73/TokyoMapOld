//
// Resources.
//

// root instance
if( !window.mapviewer ) {
  window.mapviewer = {}
};

// mapviewer.rs - Object.
//   [PROPERTY] mapviewer.lang - Language name. Currentry only "ja" (Japanese) is available. If this is not "ja", English is used.
//   [METHOD] mapviewer.get(name) - Gets the resource.
mapviewer.rs = {
  lang: "ja",
  get: function(name) {
    var ix = (this.lang == 'ja' ? 0 : 1);
    var arr = this.res[name];
    if( !arr ) {
      return null;
    }
    return arr[ix];
  },
  res: {
    "tokyo5000": ["東京5000", "Tokyo 5000"],
    "tokyo5000alt": ["東京測量図原図 (データ作成: 農業環境研究所)", "Tokyo 1:5000 scale Survey Map (Digitized by NIAES)"],
    "rapidkanto": ["迅速測図", "Rapid Kanto"],
    "rapidkantoalt": ["関東平野迅速測図 (データ作成: 農業環境研究所)", "Rapid Survey Map (Digitized by NIAES)"],
    "roadmap": ["地図", "Map"]
  }
};

