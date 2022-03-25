//
//  MapViewController.swift
//  Conversify
//
//  Created by Apple on 27/10/18.
//

import UIKit
import RxCocoa
import GoogleMaps
import RxSwift
import IBAnimatable

class MapViewController: BaseRxViewController {
    
    //MARK::- OUTLETS
    
    @IBOutlet weak var imagePrivate: UIImageView!
    @IBOutlet weak var viewMap: GMSMapView!
    @IBOutlet weak var btnJoinVenue: UIButton!
    @IBOutlet weak var imageVenue: AnimatableImageView!
    @IBOutlet weak var viewVenueDetail: UIView!
    @IBOutlet weak var searchBar: UISearchBar!
    @IBOutlet weak var labelDistance: UILabel!
    @IBOutlet weak var labelActiveNumber: UILabel!
    @IBOutlet weak var labelVenueAddress: UILabel!
    @IBOutlet weak var labelVenueName: UILabel!
    @IBOutlet weak var btnCancelSearch: UIButton!
     @IBOutlet weak var constraintWidthButtonCancel: NSLayoutConstraint!
    
    //MARK::- PROPERTIES
    var venueVM = VenueViewModal()
    var venueData = [Venues]()
    private var clusterManager: GMUClusterManager!
    var previousSelectedMarker: GMSMarker?
    var selectedIndex = -1
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        onLoad()
    }
    

    
    //MARK::- BINDINGS
    override func bindings() {
        
        guard let btnCancelSearch = btnCancelSearch else { return }
        
        btnCancelSearch.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            self?.view.endEditing(true)
            self?.searchBar.text = ""
            self?.constraintWidthButtonCancel.constant = 0
            self?.view.layoutIfNeeded()
            self?.venueVM.isSearchEnable = false
            self?.venueVM.interests.value = self?.venueVM.apiInterests.value
            self?.reloadMap()
        })<bag
        
        btnJoinVenue.rx.tap.asDriver().drive(onNext: {  [weak self] () in
            
            if /self?.venueVM.selectedVenue.value?.isMine{
                self?.venueVM.groupIdToJoin.value = self?.venueVM.selectedVenue.value?.groupId
                self?.proceedToChat()
            }else{
                 self?.venueVM.groupIdToJoin.value = self?.venueVM.selectedVenue.value?.groupId
                self?.joinVenue(groupId: /self?.venueVM.selectedVenue.value?.groupId, isPrivate: String(/self?.venueVM.selectedVenue.value?.isPrivate), adminId: /self?.venueVM.selectedVenue.value?.adminId, venue: /self?.venueVM.selectedVenue.value)
            }
            
        })<bag
        
        venueVM.pickPlace.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.getPlaceDetails()
                }
            })<bag
        
        venueVM.updatedFilteredResult.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.reloadMap()
                }
            })<bag
        
        venueVM.resetFilter.filter { (_) -> Bool in
            return true
            }.subscribe(onNext: { [weak self] (bool) in
                if bool {
                    self?.reloadMap()
                }
            })<bag
        
    }
    
    func onLoad(){
        guard let _ = viewMap else { return }
        searchBar?.delegate = self
        retrieveVenue()
        pickedLocation = { [weak self] (lat , long , name , adddress) in
            self?.venueVM.selectedFilter.value = /name.uppercaseFirst
            self?.venueVM.lat.value = lat
            self?.venueVM.long.value = long
            self?.venueVM.getFilteredVenues()
        }
    }
    
    func retrieveVenue(){
        if viewMap == nil { return }
        venueVM.retrieveVenues(){ [weak self] (status) in
            if status{
                self?.reloadMap()
            }
        }
    }
    
    func joinVenue(groupId: String , isPrivate: String , adminId: String , venue: Venues){
        guard let venueDetail = R.storyboard.venue.venueDetailViewController() else { return }
        venueDetail.detailVM = VenueDetailViewModal(venu: venue)
        venueDetail.detailVM.refreshJoined = { [weak self] in
            self?.venueData.forEach({ (venue) in
                if venue.groupId == /groupId{
                    venue.isMine = true
                }
            })
        }
        UIApplication.topViewController()?.pushVC(venueDetail)
    }
    
    func proceedToChat(){
        guard let vc = R.storyboard.chats.chatViewController() else { return }
        vc.isFromChat = true
        vc.chatModal = ChatViewModal(membersData: nil, venueD: venueVM.selectedVenue.value)
        vc.chatModal.groupIdToJoin.value = venueVM.groupIdToJoin.value
        vc.chatingType = .venue
        Singleton.sharedInstance.conversationId = /venueVM.selectedVenue.value?.conversationId
        vc.chatModal.exit = { [weak self] in
            self?.retrieveVenue()
        }
        vc.chatModal.backRefresh = { [weak self] in
            self?.venueData.forEach({ (venue) in
                if venue.groupId == /self?.venueVM.groupIdToJoin.value{
                    venue.isMine = true
                }
            })
        }
        
        pushVC(vc)
    }
    
    func reloadMap(){
        clearMapInst()
        btnJoinVenue?.isEnabled = false
        viewVenueDetail?.isHidden = true
        venueVM.interests.value?.yourVenueData?.forEach({ (venue) in
            venue.isMine = true
        })
        
        venueData = ( venueVM.interests.value?.venueNearYou ?? [] ) + ( venueVM.interests.value?.yourVenueData ?? [] )
        makeClusters()
    }
    
    func clearMapInst(){
        viewMap?.clear()
        clusterManager?.clearItems()
        
    }
    
    func makeClusters(){
        let iconGenerator = GMUDefaultClusterIconGenerator()
        let algorithm = GMUNonHierarchicalDistanceBasedAlgorithm()
        
        let renderer = GMUDefaultClusterRenderer(mapView: viewMap, clusterIconGenerator: iconGenerator)
        renderer.delegate = self
        viewMap?.mapType = .normal
        clusterManager = GMUClusterManager(map: viewMap, algorithm: algorithm, renderer: renderer)
        generateClusterItems()
        clusterManager?.cluster()
        clusterManager?.setDelegate(self, mapDelegate: self)
    }
    
    func loadMarkerDetails(index: Int){
        btnJoinVenue.isEnabled = true
        venueVM.selectedVenue.value = venueData[index]
        let venueNearMe = venueData[index]
        labelVenueName?.text = /venueNearMe.venueTitle?.uppercaseFirst
        labelVenueAddress?.text = /venueNearMe.locName + ", " + /venueNearMe.venueLocationAddress
        print(/venueNearMe.venueImageUrl?.original)
        imageVenue?.image(url:  /venueNearMe.venueImageUrl?.original, placeholder: #imageLiteral(resourceName: "ic_placeholder"))//.kf.setImage(with: URL(string: /venueNearMe.venueImageUrl?.original))
        let txt = (/venueNearMe.memberCount == 1) ? " Active Member" : " Active Members"
        labelActiveNumber?.text = /venueNearMe.memberCount?.toString + txt
        labelDistance?.text = /venueNearMe.distance?.rounded(toPlaces: 2).toString + " Mi"
        imagePrivate?.isHidden = !(/venueNearMe.isPrivate)
    }
    
    
}




//MARK::- MAPVIEWCONTROLLER

extension MapViewController :  GMUClusterManagerDelegate, GMSMapViewDelegate , GMUClusterRendererDelegate  {
    
    func mapView(_ mapView: GMSMapView, didTapAt coordinate: CLLocationCoordinate2D) {
        self.view.endEditing(true)
    }
    
    func renderer(_ renderer: GMUClusterRenderer, didRenderMarker marker: GMSMarker) {
        viewVenueDetail.isHidden = true
        if marker.userData is POIItem {
            let cMarker = GMSMarker()
            cMarker.position = marker.position
            let markerVw : CustomMarkerView = UIView.fromNib()
            marker.iconView = markerVw
            markerVw.labelName.text = (marker.userData as? POIItem )?.name
            marker.map = viewMap
            marker.zIndex = ((marker.userData as? POIItem )?.index?.toInt32) ?? 0
            if ((marker.userData as? POIItem )?.index) == selectedIndex{
                (marker.iconView as? CustomMarkerView)?.labelName.textColor = #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1)
            }
            viewMap?.selectedMarker = marker
        }
    }
    
    //GMUMapViewDelegate
        
    func mapView(_ mapView: GMSMapView, didTap overlay: GMSOverlay) {
        self.view.endEditing(true)
    }
    
    func mapView(_ mapView: GMSMapView, willMove gesture: Bool) {

    }
    
    func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
        self.view.endEditing(true)
        let newCamera = GMSCameraPosition.camera(withTarget: marker.position,
                                                 zoom: viewMap.camera.zoom)
        let update = GMSCameraUpdate.setCamera(newCamera)
        viewMap?.moveCamera(update)
        if let poiItem = marker.userData as? POIItem {
            (self.previousSelectedMarker?.iconView as? CustomMarkerView)?.backgroundColor = UIColor.clear
            self.viewMap?.selectedMarker = marker
            if selectedIndex != /poiItem.index{
                (marker.iconView as? CustomMarkerView)?.labelName.textColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
                (marker.iconView as? CustomMarkerView)?.ViewBg.backgroundColor = #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1)
                (marker.iconView as? CustomMarkerView)?.labelProtextive.backgroundColor = #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1)
                (marker.iconView as? CustomMarkerView)?.imgBeak.image = R.image.ic_marker_selected()
                selectedIndex = /poiItem.index
                btnJoinVenue?.isEnabled = true
                viewVenueDetail?.isHidden = false
                loadMarkerDetails(index: /poiItem.index)
            }else if selectedIndex == /poiItem.index{
                (marker.iconView as? CustomMarkerView)?.labelName.textColor = #colorLiteral(red: 1, green: 0.3803921569, blue: 0.1725490196, alpha: 1)
                (marker.iconView as? CustomMarkerView)?.labelProtextive.backgroundColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
                (marker.iconView as? CustomMarkerView)?.imgBeak.image = R.image.ic_marker_unselected()
                (marker.iconView as? CustomMarkerView)?.ViewBg.backgroundColor = #colorLiteral(red: 1, green: 1, blue: 1, alpha: 1)
                selectedIndex = -1
                btnJoinVenue?.isEnabled = false
                viewVenueDetail?.isHidden = true
            }
        }
        return false
    }
    
    
    internal func clusterManager(_ clusterManager: GMUClusterManager, didTap cluster: GMUCluster) -> Bool {
        self.view.endEditing(true)
        let newCamera = GMSCameraPosition.camera(withTarget: cluster.position,
                                                 zoom: viewMap.camera.zoom + 1)
        let update = GMSCameraUpdate.setCamera(newCamera)
        viewMap.moveCamera(update)
        return false
    }
    
    
    private func generateClusterItems() {
        var locations = [CLLocationCoordinate2D]()
        for (index,property) in venueData.enumerated() {
            guard let latitudeVal = property.latLong?.last as? Double , let longitudeValue =  property.latLong?.first as? Double else { return }
            let lat : CLLocationDegrees = CLLocationDegrees(latitudeVal)
            let lng = CLLocationDegrees(longitudeValue )
            locations.append(CLLocationCoordinate2D(latitude: lat, longitude: lng))
            let item = POIItem(position: CLLocationCoordinate2D(latitude: lat, longitude: lng) , name: /property.memberCount?.toString , index: index)
            clusterManager.add(item)
        }
        print(locations)
        let bounds = locations.reduce(GMSCoordinateBounds()) {
            $0.includingCoordinate($1)
        }
        viewMap.animate(with: .fit(bounds, withPadding: 30.0))
    }
    
}

class POIItem: NSObject, GMUClusterItem {
    
    var position: CLLocationCoordinate2D
    var name: String?
    var index: Int?
    
    
    init(position: CLLocationCoordinate2D, name: String , index: Int) {
        self.position = position
        self.name = name
        self.index = index
    }
    
}


//MARK::- SEARCH BAR DELEGATE
extension MapViewController : UISearchBarDelegate {
    
    func searchBar(_ searchBar: UISearchBar, textDidChange searchText: String) {
        constraintWidthButtonCancel.constant = 60
        let txt = searchText.lowercased().trimmingCharacters(in: .whitespaces)
        print(txt)
        if txt.isEmpty {
            self.venueVM.isSearchEnable = false
            self.venueVM.interests.value = self.venueVM.apiInterests.value
        }else {
            self.venueVM.isSearchEnable = true
            let searchListNearYou = /venueVM.apiInterests.value?.venueNearYou?.filter({ (/$0.venueTitle?.lowercased().contains(txt))})
            let serachListYourVenues = /venueVM.apiInterests.value?.yourVenueData?.filter({ (/$0.venueTitle?.lowercased().contains(txt))})
            let searchData = VenueData()
            searchData.venueNearYou = searchListNearYou
            searchData.yourVenueData = serachListYourVenues
            self.venueVM.interests.value = searchData
        }
        self.viewVenueDetail.isHidden = true
        btnJoinVenue.isEnabled = false
        reloadMap()
    }
    
    func searchBarSearchButtonClicked(_ searchBar: UISearchBar) {
        
    }
    
    func searchBarCancelButtonClicked(_ searchBar: UISearchBar) {
        self.venueVM.interests.value = self.venueVM.apiInterests.value
        self.view.endEditing(true)
    }
    
}
