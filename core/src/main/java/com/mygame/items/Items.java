package com.mygame.items;

public abstract class Items {
  private String Type;

  public Items(String type){
    this.Type = type;
  }

  public String getType() {
    return Type;
  }
}
