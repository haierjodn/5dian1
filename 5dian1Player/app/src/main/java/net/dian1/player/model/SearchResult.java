package net.dian1.player.model;


import java.util.List;

public class SearchResult {

	private PageInfo pageInfo;

	private List<Album> albumList;

	public PageInfo getPageInfo() {
		return pageInfo;
	}

	public void setPageInfo(PageInfo pageInfo) {
		this.pageInfo = pageInfo;
	}

	public List<Album> getAlbumList() {
		return albumList;
	}

	public void setAlbumList(List<Album> albumList) {
		this.albumList = albumList;
	}
}
