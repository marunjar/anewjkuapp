package org.voidsink.anewjkuapp.base;

import android.content.Context;
import org.voidsink.anewjkuapp.view.StickyCardListView;
import java.util.List;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public abstract class StickyCardArrayAdapter extends CardArrayAdapter implements StickyListHeadersAdapter {

    protected StickyCardListView mCardListView;

    public StickyCardArrayAdapter(Context context, List<Card> cards) {
        super(context, cards);
    }

    public void setCardListView(StickyCardListView cardListView) {
        this.mCardListView = cardListView;
    }
}