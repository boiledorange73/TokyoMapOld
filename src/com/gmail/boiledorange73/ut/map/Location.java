package com.gmail.boiledorange73.ut.map;

public class Location<CodeType> extends LonLat {
  public CodeType code;
  public String name;
  public Location() {
    super();
  }
  public Location(double lon, double lat, CodeType code, String name) {
    super(lon, lat);
    this.code = code;
    this.name = name;
  }

  @Override
  public String toString() {
    return this.name != null ? this.name : "";
  }
}

