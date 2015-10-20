package net.dian1.player.model;


import java.util.List;

public class SearchResult {

	private PageInfo pageInfo;

	private List<Music> songList;

	public PageInfo getPageInfo() {
		return pageInfo;
	}

	public void setPageInfo(PageInfo pageInfo) {
		this.pageInfo = pageInfo;
	}

	public List<Music> getSongList() {
		return songList;
	}

	public void setSongList(List<Music> songList) {
		this.songList = songList;
	}
}
