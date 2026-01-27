package com.mad.pogoenhancer.overlay;

import android.content.Context;

import androidx.annotation.Nullable;

import com.mad.shared.gpx.LatLon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import POGOProtos.Rpc.PokemonDisplayProto;

public class MonListItem extends RadarItem<PokemonDisplayProto> {

    protected int _monId;

    private static final HashSet<Integer> alolans = new HashSet<>();

    {
        if (alolans.size() == 0) {
            alolans.addAll(Arrays.asList(19, 20, 26, 27, 28, 37, 38, 51, 52, 53, 74, 75, 76, 88, 89, 103, 105));
        }
    }

    public MonListItem(PokemonDisplayProto display, LatLon location, int monId) {
        super(display, location);
        _monId = monId;
    }


    public int get_MonId() {
        return this._monId;
    }

    public int get_Form() {
        return this._representedElement.getFormValue();
    }

    public String getMonName(Context context) {
        return (String) context.getResources().getText(
                context.getResources().getIdentifier(
                        "pokemon_" + this.get_MonId(),
                        "string", "com.mad.pogoenhancer"
                )
        );
    }

    public int getMonIcon(Context context) {
        String picaddon = "";
        if (this.get_Form() > 0 && alolans.contains(this.get_MonId())) {
            picaddon = "a";
        }
        return context.getResources().getIdentifier(
                "mon_" + picaddon + String.format(Locale.ENGLISH, "%03d", this.get_MonId()),
                "drawable", "com.mad.pogoenhancer");
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (this == obj) {
            return true;
        } else if (!(obj instanceof MonListItem)) {
            return false;
        }

        MonListItem toCompareAgainst = (MonListItem) obj;

        return this.get_MonId() == toCompareAgainst.get_MonId()
                && this._location.equals(toCompareAgainst.get_Location());
    }
}
