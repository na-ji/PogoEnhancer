package com.mad.pogoenhancer.overlay.elements.nearby;

import androidx.annotation.Nullable;

import com.mad.pogoenhancer.overlay.MonListItem;
import com.mad.shared.gpx.LatLon;

import POGOProtos.Rpc.PokemonDisplayProto;

public class NearbyMonListItem extends MonListItem {
    private final long _EncounterId;

    NearbyMonListItem(PokemonDisplayProto display, LatLon location, int monId, long encounterId) {
        super(display, location, monId);
        this._EncounterId = encounterId;
    }

    public long getEncounterId() {
        return this._EncounterId;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (!(obj instanceof NearbyMonListItem)) {
            return false;
        }

        NearbyMonListItem toCompareAgainst = (NearbyMonListItem) obj;

        return this.get_MonId() == toCompareAgainst.get_MonId()
                && this._EncounterId == toCompareAgainst.getEncounterId();
    }
}
