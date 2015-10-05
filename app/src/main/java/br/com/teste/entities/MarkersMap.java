package br.com.teste.entities;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by aoliveira on 01/10/15.
 */
public class MarkersMap implements ClusterItem {


        private final LatLng mPosition;
        public final int profilePhoto;
        public final String title;

        public MarkersMap(LatLng mPosition, int profilePhoto, String title) {
            this.mPosition = mPosition;
            this.profilePhoto = profilePhoto;
            this.title = title;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }


}
