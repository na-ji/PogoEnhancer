package com.mad.pogoenhancer.overlay.elements.gyms;

import androidx.annotation.Nullable;

import com.mad.pogoenhancer.overlay.RadarItem;
import com.mad.shared.gpx.LatLon;

import POGOProtos.Rpc.PokemonFortProto;

public class GymListItem extends RadarItem<PokemonFortProto> {
    GymListItem(PokemonFortProto fortData) {
        super(fortData, new LatLon(fortData.getLatitude(), fortData.getLongitude()));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (!(obj instanceof GymListItem)) {
            return false;
        }

        GymListItem toCompareAgainst = (GymListItem) obj;

        return this._representedElement.getFortId().equals(
                toCompareAgainst.getRepresentedElement().getFortId()
        );
    }
}
