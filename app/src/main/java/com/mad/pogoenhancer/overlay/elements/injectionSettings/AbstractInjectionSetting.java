package com.mad.pogoenhancer.overlay.elements.injectionSettings;

import android.annotation.CallSuper;
import android.annotation.DrawableRes;
import android.annotation.LayoutRes;
import android.annotation.StringRes;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.AbsSavedState;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mad.pogoenhancer.Logger;
import com.mad.pogoenhancer.services.InjectionSettingSender;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.util.Set;

public abstract class AbstractInjectionSetting extends androidx.appcompat.widget.AppCompatTextView {
    private final Context mContext;

    private AbstractInjectionSetting.OnPreferenceChangeListener mOnChangeListener;
    private AbstractInjectionSetting.OnPreferenceClickListener mOnClickListener;

    private CharSequence mTitle;
    private int mTitleRes;
    private CharSequence mSummary;
    /**
     * mIconResId is overridden by mIcon, if mIcon is specified.
     */
    private int mIconResId;
    private Drawable mIcon;
    private String mKey;
    private Intent mIntent;
    private String mFragment;
    private Bundle mExtras;
    private boolean mEnabled = true;
    private boolean mSelectable = true;

    private Object mDefaultValue;

    private boolean mRecycleEnabled = true;
    private boolean mHasSingleLineTitleAttr;
    private boolean mSingleLineTitle = true;
    private boolean mIconSpaceReserved;


    protected final SharedPreferences _sharedPreferences;
    /**
     * @see #setShouldDisableView(boolean)
     */
    private boolean mShouldDisableView = true;

    private int mLayoutResId = com.android.internal.R.layout.preference;
    private int mWidgetLayoutResId;

    private AbstractInjectionSetting.OnPreferenceChangeInternalListener mListener;

    private boolean mBaseMethodCalled;

    /**
     * Interface definition for a callback to be invoked when the value of this
     * {@link AbstractInjectionSetting} has been changed by the user and is
     * about to be set and/or persisted.  This gives the client a chance
     * to prevent setting and/or persisting the value.
     */
    public interface OnPreferenceChangeListener {
        /**
         * Called when a AbstractInjectionSetting has been changed by the user. This is
         * called before the state of the AbstractInjectionSetting is about to be updated and
         * before the state is persisted.
         *
         * @param preference The changed AbstractInjectionSetting.
         * @param newValue   The new value of the AbstractInjectionSetting.
         * @return True to update the state of the AbstractInjectionSetting with the new value.
         */
        boolean onPreferenceChange(AbstractInjectionSetting preference, Object newValue);
    }

    /**
     * Interface definition for a callback to be invoked when a {@link AbstractInjectionSetting} is
     * clicked.
     */
    public interface OnPreferenceClickListener {
        /**
         * Called when a AbstractInjectionSetting has been clicked.
         *
         * @param preference The AbstractInjectionSetting that was clicked.
         * @return True if the click was handled.
         */
        boolean onPreferenceClick(AbstractInjectionSetting preference);
    }

    /**
     * Interface definition for a callback to be invoked when this
     * {@link AbstractInjectionSetting} is changed or, if this is a group, there is an
     * addition/removal of {@link AbstractInjectionSetting}(s). This is used internally.
     */
    interface OnPreferenceChangeInternalListener {
        /**
         * Called when this AbstractInjectionSettinghas changed.
         *
         * @param preference This preference.
         */
        void onPreferenceChange(AbstractInjectionSetting preference);

        /**
         * Called when this group has added/removed {@link AbstractInjectionSetting}(s).
         *
         * @param preference This Preference.
         */
        void onPreferenceHierarchyChange(AbstractInjectionSetting preference);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of AbstractInjectionSettingallows subclasses to use their own base style
     * when they are inflating. For example, a CheckBoxPreference
     * constructor calls this version of the super class constructor and
     * supplies {@code android.R.attr.checkBoxPreferenceStyle} for
     * <var>defStyleAttr</var>. This allows the theme's checkbox preference
     * style to modify all of the base preference attributes as well as the
     * CheckBoxPreference class's attributes.
     *
     * @param context      The Context this is associated with, through which it can
     *                     access the current theme, resources,
     *                     {@link SharedPreferences}, etc.
     * @param attrs        The attributes of the XML tag that is inflating the
     *                     preference.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @param defStyleRes  A resource identifier of a style resource that
     *                     supplies default values for the view, used only if
     *                     defStyleAttr is 0 or can not be found in the theme. Can be 0
     *                     to not look for defaults.
     * @see #AbstractInjectionSetting(Context, AttributeSet)
     */
    public AbstractInjectionSetting(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        _sharedPreferences = PreferenceManager.
                getDefaultSharedPreferences(context);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.Preference, defStyleAttr, defStyleRes);
        for (int i = a.getIndexCount() - 1; i >= 0; i--) {
            int attr = a.getIndex(i);
            switch (attr) {
                case com.android.internal.R.styleable.Preference_icon:
                    mIconResId = a.getResourceId(attr, 0);
                    break;

                case com.android.internal.R.styleable.Preference_key:
                    mKey = a.getString(attr);
                    break;

                case com.android.internal.R.styleable.Preference_title:
                    mTitleRes = a.getResourceId(attr, 0);
                    mTitle = a.getText(attr);
                    break;

                case com.android.internal.R.styleable.Preference_summary:
                    mSummary = a.getText(attr);
                    break;

                case com.android.internal.R.styleable.Preference_fragment:
                    mFragment = a.getString(attr);
                    break;

                case com.android.internal.R.styleable.Preference_layout:
                    mLayoutResId = a.getResourceId(attr, mLayoutResId);
                    break;

                case com.android.internal.R.styleable.Preference_widgetLayout:
                    mWidgetLayoutResId = a.getResourceId(attr, mWidgetLayoutResId);
                    break;

                case com.android.internal.R.styleable.Preference_enabled:
                    mEnabled = a.getBoolean(attr, true);
                    break;

                case com.android.internal.R.styleable.Preference_selectable:
                    mSelectable = a.getBoolean(attr, true);
                    break;

                case com.android.internal.R.styleable.Preference_defaultValue:
                    mDefaultValue = onGetDefaultValue(a, attr);
                    break;

                case com.android.internal.R.styleable.Preference_shouldDisableView:
                    mShouldDisableView = a.getBoolean(attr, mShouldDisableView);
                    break;

                case com.android.internal.R.styleable.Preference_recycleEnabled:
                    mRecycleEnabled = a.getBoolean(attr, mRecycleEnabled);
                    break;

                case com.android.internal.R.styleable.Preference_singleLineTitle:
                    mSingleLineTitle = a.getBoolean(attr, mSingleLineTitle);
                    mHasSingleLineTitleAttr = true;
                    break;

                case com.android.internal.R.styleable.Preference_iconSpaceReserved:
                    mIconSpaceReserved = a.getBoolean(attr, mIconSpaceReserved);
                    break;
            }
        }
        a.recycle();
    }

    /**
     * Perform inflation from XML and apply a class-specific base style. This
     * constructor of AbstractInjectionSettingallows subclasses to use their own base style
     * when they are inflating. For example, a CheckBoxPreference
     * constructor calls this version of the super class constructor and
     * supplies {@code android.R.attr.checkBoxPreferenceStyle} for
     * <var>defStyleAttr</var>. This allows the theme's checkbox preference
     * style to modify all of the base preference attributes as well as the
     * CheckBoxPreference class's attributes.
     *
     * @param context      The Context this is associated with, through which it can
     *                     access the current theme, resources,
     *                     {@link SharedPreferences}, etc.
     * @param attrs        The attributes of the XML tag that is inflating the
     *                     preference.
     * @param defStyleAttr An attribute in the current theme that contains a
     *                     reference to a style resource that supplies default values for
     *                     the view. Can be 0 to not look for defaults.
     * @see #AbstractInjectionSetting(Context, AttributeSet)
     */
    public AbstractInjectionSetting(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /**
     * Constructor that is called when inflating a AbstractInjectionSettingfrom XML. This is
     * called when a AbstractInjectionSettingis being constructed from an XML file, supplying
     * attributes that were specified in the XML file. This version uses a
     * default style of 0, so the only attribute values applied are those in the
     * Context's Theme and the given AttributeSet.
     *
     * @param context The Context this is associated with, through which it can
     *                access the current theme, resources, {@link SharedPreferences},
     *                etc.
     * @param attrs   The attributes of the XML tag that is inflating the
     *                preference.
     * @see #AbstractInjectionSetting(Context, AttributeSet, int)
     */
    public AbstractInjectionSetting(Context context, AttributeSet attrs) {
        this(context, attrs, com.android.internal.R.attr.preferenceStyle);
    }

    /**
     * Constructor to create a Preference.
     *
     * @param context The Context in which to store AbstractInjectionSettingvalues.
     */
    public AbstractInjectionSetting(Context context) {
        this(context, null);
    }

    /**
     * Called when a AbstractInjectionSettingis being inflated and the default value
     * attribute needs to be read. Since different AbstractInjectionSettingtypes have
     * different value types, the subclass should get and return the default
     * value which will be its value type.
     * <p>
     * For example, if the value type is String, the body of the method would
     * proxy to {@link TypedArray#getString(int)}.
     *
     * @param a     The set of attributes.
     * @param index The index of the default value attribute.
     * @return The default value of this preference type.
     */
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }

    /**
     * Sets an {@link Intent} to be used for
     * {@link Context#startActivity(Intent)} when this AbstractInjectionSettingis clicked.
     *
     * @param intent The intent associated with this Preference.
     */
    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    /**
     * Return the {@link Intent} associated with this Preference.
     *
     * @return The {@link Intent} last set via {@link #setIntent(Intent)} or XML.
     */
    public Intent getIntent() {
        return mIntent;
    }

    /**
     * Sets the class name of a fragment to be shown when this AbstractInjectionSettingis clicked.
     *
     * @param fragment The class name of the fragment associated with this Preference.
     */
    public void setFragment(String fragment) {
        mFragment = fragment;
    }

    /**
     * Return the fragment class name associated with this Preference.
     *
     * @return The fragment class name last set via {@link #setFragment} or XML.
     */
    public String getFragment() {
        return mFragment;
    }

    /**
     * Return the extras Bundle object associated with this preference, creating
     * a new Bundle if there currently isn't one.  You can use this to get and
     * set individual extra key/value pairs.
     */
    public Bundle getExtras() {
        if (mExtras == null) {
            mExtras = new Bundle();
        }
        return mExtras;
    }

    /**
     * Return the extras Bundle object associated with this preference, returning {@code null} if
     * there is not currently one.
     */
    public Bundle peekExtras() {
        return mExtras;
    }

    /**
     * Sets the layout resource that is inflated as the {@link View} to be shown
     * for this Preference. In most cases, the default layout is sufficient for
     * custom AbstractInjectionSettingobjects and only the widget layout needs to be changed.
     * <p>
     * This layout should contain a {@link ViewGroup} with ID
     * {@link android.R.id#widget_frame} to be the parent of the specific widget
     * for this Preference. It should similarly contain
     * {@link android.R.id#title} and {@link android.R.id#summary}.
     *
     * @param layoutResId The layout resource ID to be inflated and returned as
     *                    a {@link View}.
     * @see #setWidgetLayoutResource(int)
     */
    public void setLayoutResource(@LayoutRes int layoutResId) {
        if (layoutResId != mLayoutResId) {
            // Layout changed
            mRecycleEnabled = false;
        }

        mLayoutResId = layoutResId;
    }

    /**
     * Gets the layout resource that will be shown as the {@link View} for this Preference.
     *
     * @return The layout resource ID.
     */
    @LayoutRes
    public int getLayoutResource() {
        return mLayoutResId;
    }

    /**
     * Sets the layout for the controllable widget portion of this Preference. This
     * is inflated into the main layout. For example, a CheckBoxPreference
     * would specify a custom layout (consisting of just the CheckBox) here,
     * instead of creating its own main layout.
     *
     * @param widgetLayoutResId The layout resource ID to be inflated into the
     *                          main layout.
     * @see #setLayoutResource(int)
     */
    public void setWidgetLayoutResource(@LayoutRes int widgetLayoutResId) {
        if (widgetLayoutResId != mWidgetLayoutResId) {
            // Layout changed
            mRecycleEnabled = false;
        }
        mWidgetLayoutResId = widgetLayoutResId;
    }

    /**
     * Gets the layout resource for the controllable widget portion of this Preference.
     *
     * @return The layout resource ID.
     */
    @LayoutRes
    public int getWidgetLayoutResource() {
        return mWidgetLayoutResId;
    }

    /**
     * Gets the View that will be shown in the PreferenceActivity.
     *
     * @param convertView The old View to reuse, if possible. Note: You should
     *                    check that this View is non-null and of an appropriate type
     *                    before using. If it is not possible to convert this View to
     *                    display the correct data, this method can create a new View.
     * @param parent      The parent that this View will eventually be attached to.
     * @return Returns the same AbstractInjectionSettingobject, for chaining multiple calls
     * into a single statement.
     * @see #onCreateView(ViewGroup)
     * @see #onBindView(View)
     */
    public View getView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = onCreateView(parent);
        }
        onBindView(convertView);
        return convertView;
    }

    /**
     * Creates the View to be shown for this AbstractInjectionSettingin the
     * PreferenceActivity. The default behavior is to inflate the main
     * layout of this AbstractInjectionSetting(see {@link #setLayoutResource(int)}. If
     * changing this behavior, please specify a {@link ViewGroup} with ID
     * {@link android.R.id#widget_frame}.
     * <p>
     * Make sure to call through to the superclass's implementation.
     *
     * @param parent The parent that this View will eventually be attached to.
     * @return The View that displays this Preference.
     * @see #onBindView(View)
     */
    @CallSuper
    protected View onCreateView(ViewGroup parent) {
        final LayoutInflater layoutInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View layout = layoutInflater.inflate(mLayoutResId, parent, false);

        final ViewGroup widgetFrame = layout
                .findViewById(com.android.internal.R.id.widget_frame);
        if (widgetFrame != null) {
            if (mWidgetLayoutResId != 0) {
                layoutInflater.inflate(mWidgetLayoutResId, widgetFrame);
            } else {
                widgetFrame.setVisibility(View.GONE);
            }
        }
        return layout;
    }

    /**
     * Binds the created View to the data for this Preference.
     * <p>
     * This is a good place to grab references to custom Views in the layout and
     * set properties on them.
     * <p>
     * Make sure to call through to the superclass's implementation.
     *
     * @param view The View that shows this Preference.
     * @see #onCreateView(ViewGroup)
     */
    @CallSuper
    protected void onBindView(View view) {
        final TextView titleView = view.findViewById(com.android.internal.R.id.title);
        if (titleView != null) {
            final CharSequence title = getTitle();
            if (!TextUtils.isEmpty(title)) {
                titleView.setText(title);
                titleView.setVisibility(View.VISIBLE);
                if (mHasSingleLineTitleAttr) {
                    titleView.setSingleLine(mSingleLineTitle);
                }
            } else {
                titleView.setVisibility(View.GONE);
            }
        }

        final TextView summaryView = view.findViewById(
                com.android.internal.R.id.summary);
        if (summaryView != null) {
            final CharSequence summary = getSummary();
            if (!TextUtils.isEmpty(summary)) {
                summaryView.setText(summary);
                summaryView.setVisibility(View.VISIBLE);
            } else {
                summaryView.setVisibility(View.GONE);
            }
        }

        final ImageView imageView = view.findViewById(com.android.internal.R.id.icon);
        if (imageView != null) {
            if (mIconResId != 0 || mIcon != null) {
                if (mIcon == null) {
                    mIcon = getContext().getDrawable(mIconResId);
                }
                if (mIcon != null) {
                    imageView.setImageDrawable(mIcon);
                }
            }
            if (mIcon != null) {
                imageView.setVisibility(View.VISIBLE);
            } else {
                imageView.setVisibility(mIconSpaceReserved ? View.INVISIBLE : View.GONE);
            }
        }

        final View imageFrame = view.findViewById(com.android.internal.R.id.icon_frame);
        if (imageFrame != null) {
            if (mIcon != null) {
                imageFrame.setVisibility(View.VISIBLE);
            } else {
                imageFrame.setVisibility(mIconSpaceReserved ? View.INVISIBLE : View.GONE);
            }
        }

        if (mShouldDisableView) {
            setEnabledStateOnViews(view, isEnabled());
        }
    }

    /**
     * Makes sure the view (and any children) get the enabled state changed.
     */
    private void setEnabledStateOnViews(View v, boolean enabled) {
        v.setEnabled(enabled);

        if (v instanceof ViewGroup) {
            final ViewGroup vg = (ViewGroup) v;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                setEnabledStateOnViews(vg.getChildAt(i), enabled);
            }
        }
    }

    /**
     * Sets the title for this AbstractInjectionSettingwith a CharSequence. This title will be placed into the ID
     * {@link android.R.id#title} within the View created by {@link #onCreateView(ViewGroup)}.
     *
     * @param title the title for this Preference
     */
    public void setTitle(CharSequence title) {
        if (title == null && mTitle != null || title != null && !title.equals(mTitle)) {
            mTitleRes = 0;
            mTitle = title;
            notifyChanged();
        }
    }

    /**
     * Sets the title for this AbstractInjectionSettingwith a resource ID.
     *
     * @param titleResId the title as a resource ID
     * @see #setTitle(CharSequence)
     */
    public void setTitle(@StringRes int titleResId) {
        setTitle(mContext.getString(titleResId));
        mTitleRes = titleResId;
    }

    /**
     * Returns the title resource ID of this Preference. If the title did not come from a resource,
     * {@code 0} is returned.
     *
     * @return the title resource
     * @see #setTitle(int)
     */
    @StringRes
    public int getTitleRes() {
        return mTitleRes;
    }

    /**
     * Returns the title of this Preference.
     *
     * @return the title
     * @see #setTitle(CharSequence)
     */
    public CharSequence getTitle() {
        return mTitle;
    }

    /**
     * Sets the icon for this AbstractInjectionSettingwith a Drawable. This icon will be placed into the ID
     * {@link android.R.id#icon} within the View created by {@link #onCreateView(ViewGroup)}.
     *
     * @param icon the optional icon for this Preference
     */
    public void setIcon(Drawable icon) {
        if ((icon == null && mIcon != null) || (icon != null && mIcon != icon)) {
            mIcon = icon;

            notifyChanged();
        }
    }

    /**
     * Sets the icon for this AbstractInjectionSettingwith a resource ID.
     *
     * @param iconResId the icon as a resource ID
     * @see #setIcon(Drawable)
     */
    public void setIcon(@DrawableRes int iconResId) {
        if (mIconResId != iconResId) {
            mIconResId = iconResId;
            setIcon(mContext.getDrawable(iconResId));
        }
    }

    /**
     * Returns the icon of this Preference.
     *
     * @return the icon
     * @see #setIcon(Drawable)
     */
    public Drawable getIcon() {
        if (mIcon == null && mIconResId != 0) {
            mIcon = getContext().getDrawable(mIconResId);
        }
        return mIcon;
    }

    /**
     * Returns the summary of this Preference.
     *
     * @return the summary
     * @see #setSummary(CharSequence)
     */
    public CharSequence getSummary() {
        return mSummary;
    }

    /**
     * Sets the summary for this AbstractInjectionSettingwith a CharSequence.
     *
     * @param summary the summary for the preference
     */
    public void setSummary(CharSequence summary) {
        if (summary == null && mSummary != null || summary != null && !summary.equals(mSummary)) {
            mSummary = summary;
            notifyChanged();
        }
    }

    /**
     * Sets the summary for this AbstractInjectionSettingwith a resource ID.
     *
     * @param summaryResId the summary as a resource
     * @see #setSummary(CharSequence)
     */
    public void setSummary(@StringRes int summaryResId) {
        setSummary(mContext.getString(summaryResId));
    }

    /**
     * Sets whether this AbstractInjectionSettingis enabled. If disabled, it will
     * not handle clicks.
     *
     * @param enabled set {@code true} to enable it
     */
    public void setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;

            notifyChanged();
        }
    }

    /**
     * Checks whether this AbstractInjectionSetting should be enabled in the list.
     *
     * @return {@code true} if this AbstractInjectionSettingis enabled, false otherwise
     */
    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Sets whether this AbstractInjectionSettingis selectable.
     *
     * @param selectable set {@code true} to make it selectable
     */
    public void setSelectable(boolean selectable) {
        if (mSelectable != selectable) {
            mSelectable = selectable;
            notifyChanged();
        }
    }

    /**
     * Checks whether this AbstractInjectionSettingshould be selectable in the list.
     *
     * @return {@code true} if it is selectable, {@code false} otherwise
     */
    public boolean isSelectable() {
        return mSelectable;
    }

    /**
     * Sets whether this AbstractInjectionSettingshould disable its view when it gets disabled.
     *
     * <p>For example, set this and {@link #setEnabled(boolean)} to false for preferences that are
     * only displaying information and 1) should not be clickable 2) should not have the view set to
     * the disabled state.
     *
     * @param shouldDisableView set {@code true} if this preference should disable its view when
     *                          the preference is disabled
     */
    public void setShouldDisableView(boolean shouldDisableView) {
        mShouldDisableView = shouldDisableView;
        notifyChanged();
    }

    /**
     * Checks whether this AbstractInjectionSettingshould disable its view when it's action is disabled.
     *
     * @return {@code true} if it should disable the view
     * @see #setShouldDisableView(boolean)
     */
    public boolean getShouldDisableView() {
        return mShouldDisableView;
    }

    /**
     * Sets whether this AbstractInjectionSettinghas enabled to have its view recycled when used in the list
     * view. By default the recycling is enabled.
     *
     * <p>The value can be changed only before this preference is added to the preference hierarchy.
     *
     * <p>If view recycling is not allowed then each time the list view populates this preference
     * the {@link #getView(View, ViewGroup)} method receives a {@code null} convert view and needs
     * to recreate the view. Otherwise view gets recycled and only {@link #onBindView(View)} gets
     * called.
     *
     * @param enabled set {@code true} if this preference view should be recycled
     */
    @CallSuper
    public void setRecycleEnabled(boolean enabled) {
        mRecycleEnabled = enabled;
        notifyChanged();
    }

    /**
     * Checks whether this AbstractInjectionSettinghas enabled to have its view recycled when used in the list
     * view.
     *
     * @return {@code true} if this preference view should be recycled
     * @see #setRecycleEnabled(boolean)
     */
    public boolean isRecycleEnabled() {
        return mRecycleEnabled;
    }

    /**
     * Sets whether to constrain the title of this AbstractInjectionSettingto a single line instead of
     * letting it wrap onto multiple lines.
     *
     * @param singleLineTitle set {@code true} if the title should be constrained to one line
     */
    public void setSingleLineTitle(boolean singleLineTitle) {
        mHasSingleLineTitleAttr = true;
        mSingleLineTitle = singleLineTitle;
        notifyChanged();
    }

    /**
     * Gets whether the title of this preference is constrained to a single line.
     *
     * @return {@code true} if the title of this preference is constrained to a single line
     * @see #setSingleLineTitle(boolean)
     */
    public boolean isSingleLineTitle() {
        return mSingleLineTitle;
    }

    /**
     * Sets whether to reserve the space of this AbstractInjectionSettingicon view when no icon is provided.
     *
     * @param iconSpaceReserved set {@code true} if the space for the icon view should be reserved
     */
    public void setIconSpaceReserved(boolean iconSpaceReserved) {
        mIconSpaceReserved = iconSpaceReserved;
        notifyChanged();
    }

    /**
     * Gets whether the space this preference icon view is reserved.
     *
     * @return {@code true} if the space of this preference icon view is reserved
     * @see #setIconSpaceReserved(boolean)
     */
    public boolean isIconSpaceReserved() {
        return mIconSpaceReserved;
    }

    /**
     * Processes a click on the preference. This includes saving the value to
     * the {@link SharedPreferences}. However, the overridden method should
     * call {@link #callChangeListener(Object)} to make sure the client wants to
     * update the preference's state with the new value.
     */
    protected void onClick() {
    }

    /**
     * Sets the key for this Preference, which is used as a key to the {@link SharedPreferences}.
     * This should be unique for the package.
     *
     * @param key The key for the preference.
     */
    public void setKey(String key) {
        mKey = key;
    }

    /**
     * Gets the key for this Preference, which is also the key used for storing values into
     * {@link SharedPreferences}.
     *
     * @return The key.
     */
    public String getKey() {
        return mKey;
    }

    /**
     * Checks whether this AbstractInjectionSettinghas a valid key.
     *
     * @return True if the key exists and is not a blank string, false otherwise.
     */
    public boolean hasKey() {
        return !TextUtils.isEmpty(mKey);
    }

    /**
     * Call this method after the user changes the preference, but before the
     * internal state is set. This allows the client to ignore the user value.
     *
     * @param newValue The new value of this Preference.
     * @return True if the user value should be set as the preference
     * value (and persisted).
     */
    protected boolean callChangeListener(Object newValue) {
        return mOnChangeListener == null || mOnChangeListener.onPreferenceChange(this, newValue);
    }

    /**
     * Sets the callback to be invoked when this AbstractInjectionSettingis changed by the
     * user (but before the internal state has been updated).
     *
     * @param onPreferenceChangeListener The callback to be invoked.
     */
    public void setOnPreferenceChangeListener(AbstractInjectionSetting.OnPreferenceChangeListener onPreferenceChangeListener) {
        mOnChangeListener = onPreferenceChangeListener;
    }

    /**
     * Returns the callback to be invoked when this AbstractInjectionSettingis changed by the
     * user (but before the internal state has been updated).
     *
     * @return The callback to be invoked.
     */
    public AbstractInjectionSetting.OnPreferenceChangeListener getOnPreferenceChangeListener() {
        return mOnChangeListener;
    }

    /**
     * Sets the callback to be invoked when this AbstractInjectionSettingis clicked.
     *
     * @param onPreferenceClickListener The callback to be invoked.
     */
    public void setOnPreferenceClickListener(AbstractInjectionSetting.OnPreferenceClickListener onPreferenceClickListener) {
        mOnClickListener = onPreferenceClickListener;
    }

    /**
     * Returns the callback to be invoked when this AbstractInjectionSettingis clicked.
     *
     * @return The callback to be invoked.
     */
    public AbstractInjectionSetting.OnPreferenceClickListener getOnPreferenceClickListener() {
        return mOnClickListener;
    }

    /**
     * Allows a AbstractInjectionSettingto intercept key events without having focus.
     * For example, SeekBarAbstractInjectionSettinguses this to intercept +/- to adjust
     * the progress.
     *
     * @return True if the AbstractInjectionSettinghandled the key. Returns false by default.
     * @hide
     */
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    /**
     * Returns the {@link SharedPreferences} where this AbstractInjectionSetting can read its
     * value(s). Usually, it's easier to use one of the helper read methods:
     * {@link #getBoolean(boolean)}, {@link #getFloat(float)},
     * {@link #getInt(int)}, {@link #getLong(long)},
     * {@link #getString(String)}. To save values, see
     * {@link #getEditor()}.
     */
    public SharedPreferences getSharedPreferences() {
        return _sharedPreferences;
    }

    /**
     * Returns an {@link SharedPreferences.Editor} where this AbstractInjectionSettingcan
     * save its value(s). Usually it's easier to use one of the helper save
     * methods: {@link #setBoolean(boolean)}, {@link #setFloat(float)},
     * {@link #setInt(int)}, {@link #setLong(long)},
     * {@link #setString(String)}. To read values, see
     * {@link #getSharedPreferences()}.
     * <p>
     * In some cases, writes to this will not be committed right away and hence
     * not show up in the SharedPreferences, this is intended behavior to
     * improve performance.
     *
     * @return a {@link SharedPreferences.Editor} where this preference saves its value(s). If
     * this preference isn't attached to a AbstractInjectionSetting hierarchy
     * this method returns {@code null}.
     * @see #getSharedPreferences()
     */
    public SharedPreferences.Editor getEditor() {
        return _sharedPreferences.edit();
    }

    /**
     * Sets the internal change listener.
     *
     * @param listener The listener.
     * @see #notifyChanged()
     */
    final void setOnPreferenceChangeInternalListener(AbstractInjectionSetting.OnPreferenceChangeInternalListener listener) {
        mListener = listener;
    }

    /**
     * Should be called when the data of this {@link AbstractInjectionSetting} has changed.
     */
    protected void notifyChanged() {
        if (mListener != null) {
            mListener.onPreferenceChange(this);
        }
    }

    /**
     * Sets the default value for this Preference, which will be set either if
     * persistence is off or persistence is on and the preference is not found
     * in the persistent storage.
     *
     * @param defaultValue The default value.
     */
    public void setDefaultValue(Object defaultValue) {
        mDefaultValue = defaultValue;
    }

    private void dispatchSetInitialValue() {
        // By now, we know if we are persistent.
        if (!getSharedPreferences().contains(mKey)) {
            if (mDefaultValue != null) {
                onSetInitialValue(false, mDefaultValue);
            }
        } else {
            onSetInitialValue(true, null);
        }
    }

    /**
     * Implement this to set the initial value of the Preference.
     *
     * <p>If <var>restorePersistedValue</var> is true, you should restore the
     * AbstractInjectionSetting value from the {@link android.content.SharedPreferences}. If
     * <var>restorePersistedValue</var> is false, you should set the Preference
     * value to defaultValue that is given
     *
     * <p>This may not always be called. One example is if it should not persist
     * but there is no default value given.
     *
     * @param restorePersistedValue True to restore the persisted value;
     *                              false to use the given <var>defaultValue</var>.
     * @param defaultValue          The default value for this Preference. Only use this
     *                              if <var>restorePersistedValue</var> is false.
     */
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    }

    private void tryCommit(SharedPreferences.Editor editor) {
        try {
            editor.apply();
        } catch (AbstractMethodError unused) {
            // The app injected its own pre-Gingerbread
            // SharedPreferences.Editor implementation without
            // an apply method.
            editor.commit();
        }
    }

    /**
     * Attempts to persist a String if this AbstractInjectionSetting is persistent.
     *
     * @param value The value to persist.
     * @see #getString(String)
     */
    protected void setString(String value) {
        // Shouldn't store null
        if (TextUtils.equals(value, getString(null))) {
            // It's already there, so the same as persisting
            return;
        }

        // TODO: Send value to C++
        try {
            InjectionSettingSender.sendStringSetting(mKey, value);
        } catch (JSONException e) {
            Logger.debug("ProtoHookC", "Failed setting " + mKey);
        }
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putString(mKey, value);
        tryCommit(editor);
    }

    /**
     * Attempts to get a persisted String if this AbstractInjectionSetting is persistent.
     *
     * @param defaultReturnValue The default value to return if either this
     *                           AbstractInjectionSetting is not persistent or this AbstractInjectionSetting is not present.
     * @return The value from the data store or the default return
     * value.
     */
    protected String getString(String defaultReturnValue) {
        return _sharedPreferences.getString(mKey, defaultReturnValue);
    }

    /**
     * Attempts to persist a set of Strings if this AbstractInjectionSetting is persistent.
     *
     * @param values The values to persist.
     * @see #getStringSet(Set)
     */
    public void setStringSet(Set<String> values) {
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putStringSet(mKey, values);
        tryCommit(editor);
    }

    /**
     * Attempts to get a persisted set of Strings if this AbstractInjectionSetting is persistent.
     *
     * @param defaultReturnValue The default value to return if either this
     *                           AbstractInjectionSetting is not persistent or this AbstractInjectionSetting is not present.
     * @return The value from the data store or the default return
     * value.
     * @see #setStringSet(Set)
     */
    public Set<String> getStringSet(Set<String> defaultReturnValue) {
        return _sharedPreferences.getStringSet(mKey, defaultReturnValue);
    }

    /**
     * Attempts to persist an int if this AbstractInjectionSetting is persistent.
     *
     * @param value The value to persist.
     * @see #setString(String)
     * @see #getInt(int)
     */
    protected void setInt(int value) {
        if (value == getInt(~value)) {
            // It's already there, so the same as persisting
            return;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putInt(mKey, value);
        tryCommit(editor);
    }

    /**
     * Attempts to get a persisted int if this AbstractInjectionSetting is persistent.
     *
     * @param defaultReturnValue The default value to return if either this
     *                           AbstractInjectionSetting is not persistent or this AbstractInjectionSetting is not present.
     * @return The value from the data store or the default return
     * value.
     * @see #getString(String)
     * @see #setInt(int)
     */
    protected int getInt(int defaultReturnValue) {
        return _sharedPreferences.getInt(mKey, defaultReturnValue);
    }

    /**
     * Attempts to persist a long if this AbstractInjectionSetting is persistent.
     *
     * @param value The value to persist.
     * @see #setString(String)
     * @see #getFloat(float)
     */
    protected void setFloat(float value) {
        if (value == getFloat(Float.NaN)) {
            // It's already there, so the same as persisting
            return;
        }
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putFloat(mKey, value);
        tryCommit(editor);
    }

    /**
     * Attempts to get a persisted float if this AbstractInjectionSetting is persistent.
     *
     * @param defaultReturnValue The default value to return if either this
     *                           AbstractInjectionSetting is not persistent or this AbstractInjectionSetting is not present.
     * @return The value from the data store or the default return
     * value.
     * @see #getString(String)
     * @see #setFloat(float)
     */
    protected float getFloat(float defaultReturnValue) {
        return _sharedPreferences.getFloat(mKey, defaultReturnValue);
    }

    /**
     * Attempts to persist a long if this AbstractInjectionSetting is persistent.
     *
     * @param value The value to persist.
     * @see #setString(String)
     * @see #getLong(long)
     */
    protected void setLong(long value) {
        if (value == getLong(~value)) {
            // It's already there, so the same as persisting
            return;
        }
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putLong(mKey, value);
        tryCommit(editor);
    }

    /**
     * Attempts to get a persisted long if this AbstractInjectionSetting is persistent.
     *
     * @param defaultReturnValue The default value to return if either this
     *                           AbstractInjectionSetting is not persistent or this AbstractInjectionSetting is not present.
     * @return The value from the data store or the default return
     * value.
     * @see #getString(String)
     * @see #setLong(long)
     */
    protected long getLong(long defaultReturnValue) {
        return _sharedPreferences.getLong(mKey, defaultReturnValue);
    }

    /**
     * Attempts to persist a boolean if this AbstractInjectionSetting is persistent.
     *
     * @param value The value to persist.
     * @return True if this AbstractInjectionSetting is persistent. (This is not whether the
     * value was persisted, since we may not necessarily commit if there
     * will be a batch commit later.)
     * @see #getString(String)
     * @see #getBoolean(boolean)
     */
    protected void setBoolean(boolean value) {
        if (value == getBoolean(!value)) {
            // It's already there, so the same as persisting
            return;
        }

        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putBoolean(mKey, value);
        tryCommit(editor);
    }

    /**
     * Attempts to get a persisted boolean if this AbstractInjectionSetting is persistent.
     *
     * @param defaultReturnValue The default value to return if either this
     *                           AbstractInjectionSetting is not persistent or this AbstractInjectionSetting is not present.
     * @return The value from the data store or the default return
     * value.
     * @see #getString(String)
     * @see #setBoolean(boolean)
     */
    protected boolean getBoolean(boolean defaultReturnValue) {
        return _sharedPreferences.getBoolean(mKey, defaultReturnValue);
    }

    @Override
    public @NotNull String toString() {
        return getFilterableStringBuilder().toString();
    }

    /**
     * Returns the text that will be used to filter this AbstractInjectionSetting depending on
     * user input.
     * <p>
     * If overridding and calling through to the superclass, make sure to prepend
     * your additions with a space.
     *
     * @return Text as a {@link StringBuilder} that will be used to filter this
     * preference. By default, this is the title and summary
     * (concatenated with a space).
     */
    StringBuilder getFilterableStringBuilder() {
        StringBuilder sb = new StringBuilder();
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            sb.append(title).append(' ');
        }
        CharSequence summary = getSummary();
        if (!TextUtils.isEmpty(summary)) {
            sb.append(summary).append(' ');
        }
        if (sb.length() > 0) {
            // Drop the last space
            sb.setLength(sb.length() - 1);
        }
        return sb;
    }

    /**
     * A base class for managing the instance state of a {@link AbstractInjectionSetting}.
     */
    public static class BaseSavedState extends AbsSavedState {
        public BaseSavedState(Parcel source) {
            super(source);
        }

        public BaseSavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<AbstractInjectionSetting.BaseSavedState> CREATOR =
                new Parcelable.Creator<AbstractInjectionSetting.BaseSavedState>() {
                    public AbstractInjectionSetting.BaseSavedState createFromParcel(Parcel in) {
                        return new AbstractInjectionSetting.BaseSavedState(in);
                    }

                    public AbstractInjectionSetting.BaseSavedState[] newArray(int size) {
                        return new AbstractInjectionSetting.BaseSavedState[size];
                    }
                };
    }

}
