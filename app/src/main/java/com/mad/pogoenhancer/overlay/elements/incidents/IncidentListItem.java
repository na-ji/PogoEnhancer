package com.mad.pogoenhancer.overlay.elements.incidents;

import androidx.annotation.Nullable;

import com.mad.pogoenhancer.overlay.RadarItem;
import com.mad.shared.gpx.LatLon;

import POGOProtos.Rpc.PokemonFortProto;

public class IncidentListItem extends RadarItem<PokemonFortProto> {
    IncidentListItem(PokemonFortProto fortData) {
        super(fortData, new LatLon(fortData.getLatitude(), fortData.getLongitude()));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (!(obj instanceof IncidentListItem)) {
            return false;
        }

        IncidentListItem toCompareAgainst = (IncidentListItem) obj;

        return this._representedElement.getFortId().equals(
                toCompareAgainst.getRepresentedElement().getFortId()
        );
    }
}
