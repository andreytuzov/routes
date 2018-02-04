package ru.railway.dc.routes.search.core.model;

public class IndexRoute {

	private long iBTime;
	private long iETime;
	private int iRoute;
	
	public IndexRoute(long iBTime, long iETime, int iRoute) {
		this.iBTime = iBTime;
		this.iETime = iETime;
		this.iRoute = iRoute;
	}

	@Override
	public String toString() {
		return "\niBTime = " + iBTime 
				+ ", iETime = " + iETime
				+ ", iRoute = " + iRoute;
	}
	
	public long getiBTime() {
		return iBTime;
	}

	public void setiBTime(long iBTime) {
		this.iBTime = iBTime;
	}

	public long getiETime() {
		return iETime;
	}

	public void setiETime(long iETime) {
		this.iETime = iETime;
	}

	public int getiLRoute() {
		return iRoute;
	}

	public void setiLRoute(int iRoute) {
		this.iRoute = iRoute;
	}
}
