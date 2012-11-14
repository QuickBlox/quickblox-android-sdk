package com.quickblox.sample.location.objects;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/**
 * Date: 1.11.12
 * Time: 12:16
 */

/**
 * Custom map marker
 *
 * @author <a href="mailto:igor@quickblox.com">Igor Khomenko</a>
 */
public class MapOverlayItem extends OverlayItem{

	private String userName;
	private String userStatus;

	public MapOverlayItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getUserStatus() {
		return userStatus;
	}

	public void setUserStatus(String userStatus) {
		this.userStatus = userStatus;
	}
}