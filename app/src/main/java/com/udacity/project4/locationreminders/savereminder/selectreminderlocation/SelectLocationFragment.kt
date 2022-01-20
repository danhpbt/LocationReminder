package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.isPermissionGranted
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    companion object{
        val REQUEST_ACCESS_FINE_LOCATION_CODE = 9879
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private var googleMap: GoogleMap? = null
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var reminderSelectedLocationStr = ""
    private lateinit var selectedPOI: PointOfInterest

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        requestPermissions()

        binding.btnSave.setOnClickListener { onLocationSelected() }

        return binding.root
    }

    private fun onLocationSelected() {
        if (reminderSelectedLocationStr.isNotBlank()){
            _viewModel.reminderSelectedLocationStr.value = reminderSelectedLocationStr
            _viewModel.selectedPOI.value = selectedPOI
            _viewModel.latitude.value = selectedPOI.latLng.latitude
            _viewModel.longitude.value = selectedPOI.latLng.longitude
            _viewModel.navigationCommand.value = NavigationCommand.Back
        }
        else
            _viewModel.showToast.value = getString(R.string.err_select_location)

    }

    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0
        googleMap?.let {
            setupGoogleMap(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.d("onRequestPermissionResult")

        if (
            grantResults.isEmpty() ||
            grantResults[0] == PackageManager.PERMISSION_DENIED
        ) {
            _viewModel.showErrorMessage.value = getString(R.string.location_required_error)
        } else {
            googleMap?.let {
                setupGoogleMap(it)
            }
        }
    }

    private fun requestPermissions() {
        if (!isPermissionGranted(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            val resultCode = REQUEST_ACCESS_FINE_LOCATION_CODE

            requestPermissions(
                permissionsArray,
                resultCode
            )
        }
    }

    private fun setupGoogleMap(map: GoogleMap)
    {
        if (isPermissionGranted(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            map.isMyLocationEnabled = true
            map.uiSettings?.isMyLocationButtonEnabled = true

            setMapStyle(map)
            zoomToCurrentLocation(map)
            setMapLongClick(map)
            setPoiClick(map)
        }

    }

    private fun zoomToCurrentLocation(map: GoogleMap) {
        fusedLocationProviderClient?.lastLocation?.addOnSuccessListener(requireActivity()) {
            it?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        latLng,
                        17f
                    )
                )
            }
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Timber.e("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Timber.e("Can't find style. Error: ")
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            map.clear()
            val snippet = String.format(
                Locale.getDefault(),
                getString(R.string.lat_long_snippet),
                latLng.latitude,
                latLng.longitude
            )
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            )

            poiMarker.showInfoWindow()
            selectedPOI = PointOfInterest(latLng, getString(R.string.my_selected_location),
                getString(R.string.dropped_pin))
            reminderSelectedLocationStr = selectedPOI.name
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()
            selectedPOI = poi
            reminderSelectedLocationStr = poi.name
        }
    }

}
