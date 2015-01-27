package org.voidsink.anewjkuapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.voidsink.anewjkuapp.base.ListWithHeaderAdapter;
import org.voidsink.anewjkuapp.mensa.Mensa;
import org.voidsink.anewjkuapp.mensa.MensaDay;
import org.voidsink.anewjkuapp.mensa.MensaMenu;
import org.voidsink.anewjkuapp.utils.UIUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MensaMenuAdapter extends ListWithHeaderAdapter<MensaItem> {

    private static final DateFormat df = SimpleDateFormat.getDateInstance();
    protected boolean mUseDateHeader;

    public MensaMenuAdapter(Context context, int textViewResourceId, boolean useDateHeader) {
        super(context, textViewResourceId);

        this.mUseDateHeader = useDateHeader;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MensaItem item = this.getItem(position);
        if (item == null) {
            return null;
        }

        switch (item.getType()) {
            case MensaItem.TYPE_MENU:
                return getMenuView(convertView, parent, item);
            case MensaItem.TYPE_INFO:
                return getInfoView(convertView, parent, item);
            default:
                return null;
        }
    }

    private View getInfoView(View convertView, ViewGroup parent, MensaItem item) {
        MensaInfoItem mensaInfoItem = (org.voidsink.anewjkuapp.MensaInfoItem) item;
        MensaInfoHolder mensaInfoItemHolder = null;

        if (convertView == null ||
                !(convertView.getTag() instanceof MensaInfoHolder)) {
            final LayoutInflater mInflater = LayoutInflater.from(getContext());
            convertView = mInflater.inflate(R.layout.mensa_info_item, parent,
                    false);
            mensaInfoItemHolder = new MensaInfoHolder();
            mensaInfoItemHolder.title = (TextView) convertView
                    .findViewById(R.id.mensa_info_item_title);
            mensaInfoItemHolder.descr = (TextView) convertView
                    .findViewById(R.id.mensa_info_item_descr);

            convertView.setTag(mensaInfoItemHolder);
        }

        if (mensaInfoItemHolder == null) {
            mensaInfoItemHolder = (MensaInfoHolder) convertView.getTag();
        }

        mensaInfoItemHolder.title.setText(mensaInfoItem.getTitle());
        UIUtils.setTextAndVisibility(mensaInfoItemHolder.descr, mensaInfoItem.getDescr());

        return convertView;
    }

    private View getMenuView(View convertView, ViewGroup parent, MensaItem item) {
        MensaMenu mensaMenuItem = (MensaMenu) item;
        MensaMenuHolder mensaMenuItemHolder = null;

        if (convertView == null || !(convertView.getTag() instanceof MensaMenuHolder)) {
            final LayoutInflater mInflater = LayoutInflater.from(getContext());
            convertView = mInflater.inflate(R.layout.mensa_menu_item, parent,
                    false);
            mensaMenuItemHolder = new MensaMenuHolder();

            mensaMenuItemHolder.name = (TextView) convertView
                    .findViewById(R.id.mensa_menu_item_name);
            mensaMenuItemHolder.soup = (TextView) convertView
                    .findViewById(R.id.mensa_menu_item_soup);
            mensaMenuItemHolder.meal = (TextView) convertView
                    .findViewById(R.id.mensa_menu_item_meal);
            mensaMenuItemHolder.price = (TextView) convertView
                    .findViewById(R.id.mensa_menu_item_price);
            mensaMenuItemHolder.oehBonus = (TextView) convertView
                    .findViewById(R.id.mensa_menu_item_oeh_bonus);

            convertView.setTag(mensaMenuItemHolder);
        }

        if (mensaMenuItemHolder == null) {
            mensaMenuItemHolder = (MensaMenuHolder) convertView.getTag();
        }

        UIUtils.setTextAndVisibility(mensaMenuItemHolder.name, mensaMenuItem.getName());
        UIUtils.setTextAndVisibility(mensaMenuItemHolder.soup, mensaMenuItem.getSoup());

        mensaMenuItemHolder.meal.setText(mensaMenuItem.getMeal());
        if (mensaMenuItem.getPrice() > 0) {
            mensaMenuItemHolder.price.setText(String.format("%.2f €",
                    mensaMenuItem.getPrice()));
            mensaMenuItemHolder.price.setVisibility(View.VISIBLE);

            if (mensaMenuItem.getOehBonus() > 0) {
                mensaMenuItemHolder.oehBonus.setText(String.format(
                        "inkl %.2f € ÖH Bonus", mensaMenuItem.getOehBonus()));
                mensaMenuItemHolder.oehBonus.setVisibility(View.VISIBLE);
            } else {
                mensaMenuItemHolder.oehBonus.setVisibility(View.GONE);
            }
        } else {
            mensaMenuItemHolder.price.setVisibility(View.GONE);
            mensaMenuItemHolder.oehBonus.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return this.getItem(position).getType();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup viewGroup) {
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View headerView = mInflater.inflate(R.layout.list_header, viewGroup, false);

        MensaItem item = getItem(position);
        if (item != null) {
            final TextView tvHeaderTitle = (TextView) headerView.findViewById(R.id.list_header_text);
            if (mUseDateHeader) {
                final MensaDay day = item.getDay();
                if (day != null) {
                    tvHeaderTitle.setText(df.format(day.getDate()));
                }
            } else {
                Mensa mensa = item.getMensa();
                if (mensa != null) {
                    tvHeaderTitle.setText(mensa.getName());
                }
            }
        }
        return headerView;
    }

    @Override
    public long getHeaderId(int position) {
        MensaItem item = getItem(position);
        if (item != null) {
            if (mUseDateHeader) {
                final MensaDay day = item.getDay();
                if (day != null) {
                    Calendar cal = Calendar.getInstance(); // locale-specific
                    cal.setTimeInMillis(day.getDate().getTime());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    return cal.getTimeInMillis();
                }
            } else {
                Mensa mensa = item.getMensa();
                if (mensa != null) {
                    return mensa.getName().hashCode();
                }
            }
        }
        return 0;
    }

    private class MensaMenuHolder {
        public TextView name;
        public TextView soup;
        public TextView meal;
        public TextView price;
        public TextView oehBonus;
    }

    private class MensaInfoHolder {
        public TextView title;
        public TextView descr;
    }
}
