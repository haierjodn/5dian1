package net.dian1.player.model;


/**
 * Created by Six.SamA on 2015/9/6.
 * Email:MephistoLake@gmail.com
 */
public class PageInfo {

	private int pageNum;

	private int pageSize;

	private int total;

	public int getPageNum() {
		return pageNum;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}
}
