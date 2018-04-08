package info.emm.ui;

import info.emm.messenger.LocaleController;
import info.emm.messenger.MessagesController;
import info.emm.messenger.NotificationCenter;
import info.emm.messenger.TLRPC;
import info.emm.objects.MessageObject;
import info.emm.ui.Views.BackupImageView;
import info.emm.ui.Views.BaseFragment;
import info.emm.utils.Utilities;
import info.emm.yuanchengcloudb.R;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.CameraUpdate;

public class AmapLocationActivity extends BaseFragment implements AMapLocationListener,NotificationCenter.NotificationCenterDelegate,LocationSource{
	private LocationManagerProxy locationManager;
	private OnLocationChangedListener mListener;
	private Location myLocation;
    private Location userLocation;
	private Marker userMarker;
	private MapView mapView;
	private AMap aMap;

	private MessageObject messageObject;
	private View bottomView;
	private TextView nameTextView;
    private TextView sendButton;
    private TextView distanceTextView;
    private BackupImageView avatarImageView;
    private boolean userLocationMoved = false;
    private boolean firstWas = false;
	
	@Override
    public boolean onFragmentCreate() 
	{
        super.onFragmentCreate();
        messageObject = (MessageObject)NotificationCenter.getInstance().getFromMemCache(0);
        NotificationCenter.getInstance().addObserver(this, MessagesController.closeChats);
        if (messageObject != null) 
        {
            NotificationCenter.getInstance().addObserver(this, MessagesController.updateInterfaces);
        }
        
        return true;
    }

    @Override
    public void onFragmentDestroy() 
    {
        super.onFragmentDestroy();
        NotificationCenter.getInstance().removeObserver(this, MessagesController.updateInterfaces);
        NotificationCenter.getInstance().removeObserver(this, MessagesController.closeChats);
        
        if (locationManager != null) 
        {
			locationManager.removeUpdates(this);
			locationManager.destory();
		}
		locationManager = null;
    }
    
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

	@Override
    public void applySelfActionBar() 
	{
        if (parentActivity == null) 
        {
            return;
        }
       
        ActionBar actionBar =  super.applySelfActionBar(true);
        if (messageObject != null) 
        {
            actionBar.setTitle(LocaleController.getString("ChatLocation", R.string.ChatLocation));
        } 
        else 
        {
            actionBar.setTitle(LocaleController.getString("ShareLocation", R.string.ShareLocation));
        }

        TextView title = (TextView)parentActivity.findViewById(R.id.action_bar_title);
        if (title == null) 
        {
            final int subtitleId = parentActivity.getResources().getIdentifier("action_bar_title", "id", "android");
            title = (TextView)parentActivity.findViewById(subtitleId);
        }
        if (title != null) 
        {
            title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            title.setCompoundDrawablePadding(0);
        }
    }

    @Override
    public void onResume() 
    {
        super.onResume();
        
        mapView.onResume();

        if (getActivity() == null) 
        {
            return;
        }
        ((LaunchActivity)parentActivity).showActionBar();
        ((LaunchActivity)parentActivity).updateActionBar();
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
        if (fragmentView == null) 
        {
            if (messageObject != null) 
            {
                fragmentView = inflater.inflate(R.layout.amap_location_view_layout, container, false);
            } 
            else 
            {
                fragmentView = inflater.inflate(R.layout.amap_location_attach_layout, container, false);
            }
            
            mapView = (MapView)fragmentView.findViewById(R.id.map);
            mapView.onCreate(savedInstanceState);
            
            avatarImageView = (BackupImageView)fragmentView.findViewById(R.id.location_avatar_view);
            nameTextView = (TextView)fragmentView.findViewById(R.id.location_name_label);
            distanceTextView = (TextView)fragmentView.findViewById(R.id.location_distance_label);
            bottomView = fragmentView.findViewById(R.id.location_bottom_view);
            sendButton = (TextView)fragmentView.findViewById(R.id.location_send_button);
            if (sendButton != null) 
            {
                sendButton.setText(LocaleController.getString("SendLocation", R.string.SendLocation));
                sendButton.setOnClickListener(new View.OnClickListener() 
                {
                    @Override
                    public void onClick(View view) 
                    {
                    	NotificationCenter.getInstance().postNotificationName(997, userLocation.getLatitude(), userLocation.getLongitude());
                        finishFragment();
                    }
                });
            }
        } 
        else 
        {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) 
            {
                parent.removeView(fragmentView);
            }
        }
        
        init();
        return fragmentView;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
    {
        inflater.inflate(R.menu.amap_location_menu, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) 
    {
        int itemId = item.getItemId();
        if (itemId == R.id.map_list_menu_map) {
            if (aMap != null) {
                aMap.setMapType(AMap.MAP_TYPE_NORMAL);
            }

        } else if (itemId == R.id.map_list_menu_satellite) {
            if (aMap != null) {
                aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
            }

        } else if (itemId == R.id.map_to_my_location) {
            if (myLocation != null) {
                LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                if (aMap != null) {
                    CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, aMap.getMaxZoomLevel() - 4);
                    aMap.animateCamera(position);
                }
            }

        } else if (itemId == android.R.id.home) {
            finishFragment();

        }
        return true;
    }
    
    private void updateUserData() 
    {
        if (messageObject != null && avatarImageView != null) 
        {
            int fromId = messageObject.messageOwner.from_id;
            if (messageObject.messageOwner instanceof TLRPC.TL_messageForwarded) 
            {
                fromId = messageObject.messageOwner.fwd_from_id;
            }
            TLRPC.User user = MessagesController.getInstance().users.get(fromId);
            if (user != null) 
            {
            	String nameTextString = Utilities.formatName(user);
                TLRPC.FileLocation photo = null;
                if (user.photo != null) 
                {
                    photo = user.photo.photo_small;
                }
                avatarImageView.setImage(photo, null, Utilities.getUserAvatarForId(user.id));
                nameTextView.setText(nameTextString);
            }
        }
    }
    
    @Override
    public void didReceivedNotification(int id, Object... args) 
    {
        if (id == MessagesController.updateInterfaces) 
        {
            int mask = (Integer)args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0) 
            {
                updateUserData();
            }
        } 
        else if (id == MessagesController.closeChats) 
        {
            removeSelfFromStack();
        }
    }
    
	private void init() 
	{
		if (aMap == null) 
		{
			aMap = mapView.getMap();
			setUpMap();
		}
		
		InitView();
	}
	
	private void InitView()
	{
		if (sendButton != null && aMap != null) 
		{
			userLocation = new Location("network");
	        userLocation.setLatitude(20.659322);
	        userLocation.setLongitude(-11.406250);
	        LatLng latLng = new LatLng(20.659322, -11.406250);
	        userMarker = aMap.addMarker(new MarkerOptions().position(latLng).
	        icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)).draggable(true));
	        
	        aMap.setOnMarkerDragListener(new AMap.OnMarkerDragListener() 
	        {
                @Override
                public void onMarkerDragStart(Marker marker) 
                {
                }

                @Override
                public void onMarkerDrag(Marker marker) 
                {
                    userLocationMoved = true;
                }

                @Override
                public void onMarkerDragEnd(Marker marker) 
                {
                    LatLng latLng = marker.getPosition();
                    userLocation.setLatitude(latLng.latitude);
                    userLocation.setLongitude(latLng.longitude);
                }
            });
	        
		}
		
		if (bottomView != null  && aMap != null) 
		{
            bottomView.setOnClickListener(new View.OnClickListener() 
            {
                @Override
                public void onClick(View view) 
                {
                    if (userLocation != null) 
                    {
                        LatLng latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
                        CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, aMap.getMaxZoomLevel() - 4);
                        aMap.animateCamera(position);
                    }
                }
            });
        }
		
		if (messageObject != null  && aMap != null) 
		{
            int fromId = messageObject.messageOwner.from_id;
            if (messageObject.messageOwner instanceof TLRPC.TL_messageForwarded) 
            {
                fromId = messageObject.messageOwner.fwd_from_id;
            }
            TLRPC.User user = MessagesController.getInstance().users.get(fromId);
            if (user != null) 
            {
                avatarImageView.setImage(user.photo.photo_small, "50_50", Utilities.getUserAvatarForId(user.id));
                nameTextView.setText(Utilities.formatName(user.first_name, user.last_name));
            }
            
            userLocation = new Location("network");
            userLocation.setLatitude(messageObject.messageOwner.media.geo.lat);
            userLocation.setLongitude(messageObject.messageOwner.media.geo._long);
            LatLng latLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            userMarker = aMap.addMarker(new MarkerOptions().position(latLng).
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)));
            CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, aMap.getMaxZoomLevel() - 4);
            aMap.animateCamera(position);
        }
		
		positionMarker(myLocation);

        ViewGroup topLayout = (ViewGroup)parentActivity.findViewById(R.id.container);
        topLayout.requestTransparentRegion(topLayout);
	}

	private void setUpMap() 
	{
		aMap.setLocationSource(this);// ���ö�λ����
		aMap.getUiSettings().setMyLocationButtonEnabled(false);// ����Ĭ�϶�λ��ť�Ƿ���ʾ
		aMap.setMyLocationEnabled(true);// ����Ϊtrue��ʾ��ʾ��λ�㲢�ɴ�����λ��false��ʾ���ض�λ�㲢���ɴ�����λ��Ĭ����false
		
		aMap.getUiSettings().setZoomControlsEnabled(false); // �������Ű�ť�Ƿ���ʾ
		myLocation = aMap.getMyLocation();
	}
	
	@Override
	public void onPause() 
	{
		super.onPause();
		mapView.onPause();
		deactivate();
		
		if (locationManager != null) 
		{
			locationManager.removeUpdates(this);
			locationManager.destory();
		}
		locationManager = null;
	}
	
	@Override
	public void onLocationChanged(Location location) 
	{

	}

	@Override
	public void onProviderDisabled(String provider) 
	{

	}

	@Override
	public void onProviderEnabled(String provider) 
	{

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{

	}

	@Override
	public void onLocationChanged(AMapLocation location) 
	{
		if (mListener != null && location != null) 
		{
			positionMarker(location);

		}
	}

	@Override
	public void activate(OnLocationChangedListener listener) 
	{
		// TODO Auto-generated method stub
		mListener = listener;
		if (locationManager == null) 
		{
			locationManager = LocationManagerProxy.getInstance(AmapLocationActivity.this.getActivity());
			locationManager.requestLocationUpdates(LocationProviderProxy.AMapNetwork, 2000, 10, this);
		}
	}

	@Override
	public void deactivate() 
	{
		// TODO Auto-generated method stub
		mListener = null;
		if (locationManager != null) 
		{
			locationManager.removeUpdates(this);
			locationManager.destory();
		}
		locationManager = null;
	}
	
	private void positionMarker(Location location) 
	{
        if (location == null) 
        {
            return;
        }
        myLocation = location;
        if (messageObject != null) 
        {
            if (userLocation != null && distanceTextView != null) 
            {
                float distance = location.distanceTo(userLocation);
                if (distance < 1000) 
                {
                    distanceTextView.setText(String.format("%d %s", (int)(distance), LocaleController.getString("MetersAway", R.string.MetersAway)));
                } 
                else 
                {
                    distanceTextView.setText(String.format("%.2f %s", distance / 1000.0f, LocaleController.getString("KMetersAway", R.string.KMetersAway)));
                }
            }
        } 
        else 
        {
            if (!userLocationMoved && aMap != null) 
            {
            	if (userLocation.getLatitude() == location.getLatitude() && 
					userLocation.getLongitude() == location.getLongitude())
            	{
            		return;
				}
                userLocation = location;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                userMarker.setPosition(latLng);
                if (firstWas) 
                {
                    CameraUpdate position = CameraUpdateFactory.newLatLng(latLng);
                    aMap.animateCamera(position);
                } 
                else 
                {
                    firstWas = true;
                    CameraUpdate position = CameraUpdateFactory.newLatLngZoom(latLng, aMap.getMaxZoomLevel() - 4);
                    aMap.animateCamera(position);
                }
            }
        }

    }
}