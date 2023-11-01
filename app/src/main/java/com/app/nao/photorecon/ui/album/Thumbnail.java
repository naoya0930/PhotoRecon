package com.app.nao.photorecon.ui.album;

import java.util.List;

class Thumbnail{
    public List<String> thumbnailImageUris; // 複数の画像 URI グループの文字列形式
    public List<String> thumbnailClassnameLists;
    Thumbnail(List<String> thumbnailImageUris,List<String> thumbnailClassnameLists) {
        this.thumbnailClassnameLists=thumbnailClassnameLists;
        this.thumbnailImageUris=thumbnailImageUris;
    }
}