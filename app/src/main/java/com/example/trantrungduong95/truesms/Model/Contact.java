package com.example.trantrungduong95.truesms.Model;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;

import com.example.trantrungduong95.truesms.Model.Wrapper.ContactsWrapper;

// Information about a link Contact.
public class Contact {

	//link Contact's number.
	public String mNumber;

	//link Contact's name.
	public String mName;

	//link Contact's name and number formated like "name &lt;number&gt;.
	private String mNameAndNumber;

	//link Contact's recipient id.
	public long mRecipientId;

	//link Contact's person id.
	public long mPersonId;

	//link Contact's lookup key.
	public String mLookupKey;

	//link Contact's presence state.
	public int mPresenceState;

	//link Contact's presence text.
	public String mPresenceText;

	//link Contact's avatar.
	public BitmapDrawable mAvatar;

	//link Contact's avatar data.
	public byte[] mAvatarData;

	//link Uri to link Contact.
	private Uri mContactUri = null;

	//Lookup link Uri to link Contact.
	private Uri mLookupUri = null;

	/**
	 * Get default {@link Drawable} resource id for {@link Contact}'s presence state.
	 *
	 * @param presenceState presence state
	 * @return {@link Drawable}'s resId
	 */
	public static int getPresenceRes(int presenceState) {
		switch (presenceState) {
			case ContactsWrapper.PRESENCE_STATE_AVAILABLE:
				return android.R.drawable.presence_online;
			case ContactsWrapper.PRESENCE_STATE_AWAY:
			case ContactsWrapper.PRESENCE_STATE_IDLE:
			case ContactsWrapper.PRESENCE_STATE_INVISIBLE:
				return android.R.drawable.presence_away;
			case ContactsWrapper.PRESENCE_STATE_DO_NOT_DISTURB:
				return android.R.drawable.presence_busy;
			case ContactsWrapper.PRESENCE_STATE_OFFLINE:
				return android.R.drawable.presence_offline;
			default:
				return -1;
		}
	}

	public Contact(long recipientId) {
		this.mPersonId = -1L;
		this.mRecipientId = recipientId;
	}

	public Contact(long recipientId, String number) {
		this.mPersonId = -1L;
		this.mRecipientId = recipientId;
		this.mNumber = number;
		this.updateNameAndNumer();
	}

	public Contact(String number) {
		this.mPersonId = -1L;
		this.mRecipientId = -1L;
		this.mNumber = number;
		this.updateNameAndNumer();
	}

	public Contact(String number, String name) {
		this.mNumber = number;
		 this.mName = name;
	}


	/**
	 * Update {@link Contact}'s details.
	 *
	 * @param context    {@link Context}
	 * @param loadOnly   load only data which is not available
	 * @param loadAvatar load avatar?
	 * @return true if {@link Contact}'s details where changed
	 */
	public boolean update(Context context, boolean loadOnly, boolean loadAvatar) {
		return ContactsWrapper.getInstance().updateContactDetails(context, loadOnly, loadAvatar, this);
	}

	public String getNumber() {
		return this.mNumber;
	}

	public void setNumber(String number) {
		this.mNumber = number;
		this.updateNameAndNumer();
	}

	public void updateNameAndNumer() {
		String name = this.mName;
		String number = this.mNumber;
		if (TextUtils.isEmpty(name)) {
			if (TextUtils.isEmpty(number)) {
				this.mNameAndNumber = "...";
			} else {
				this.mNameAndNumber = PhoneNumberUtils.formatNumber(number);
			}
		} else {
			if (TextUtils.isEmpty(number)) {
				this.mNameAndNumber = name;
			} else {
				this.mNameAndNumber = name + " <" + PhoneNumberUtils.formatNumber(number) + ">";
			}
		}
	}

	public String getName() {
		return this.mName;
	}

	public void setName(String name) {
		this.mName = name;
		this.updateNameAndNumer();
	}

	public String getNameAndNumber() {
		return this.mNameAndNumber;
	}

	public String getDisplayName() {
		if (TextUtils.isEmpty(this.mName)) {
			if (TextUtils.isEmpty(this.mNumber)) {
				return "...";
			} else {
				return this.mNumber;
			}
		} else {
			return this.mName;
		}
	}

	public long getRecipientId() {
		return this.mRecipientId;
	}

	public long getContactId() {
		return this.mPersonId;
	}

	public String getLookUpKey() {
		return this.mLookupKey;
	}

	public Uri getUri() {
		if (this.mContactUri == null && this.mPersonId > 0L) {
			this.mContactUri = ContactsWrapper.getInstance().getContactUri(this.mPersonId);
		}
		return this.mContactUri;
	}

	public Uri getLookUpUri(ContentResolver cr) {
		if (this.mLookupUri == null && this.mLookupKey != null) {
			this.mLookupUri = ContactsWrapper.getInstance().getLookupUri(cr, this.mLookupKey);
		}
		return this.mLookupUri;
	}

	public int getPresenceState() {
		return this.mPresenceState;
	}

	public String getPresenceText() {
		return this.mPresenceText;
	}

	public Drawable getAvatar(Context context, Drawable defaultValue) {
		if (this.mAvatar == null) {
			if (this.mAvatarData != null) {
				Bitmap b = BitmapFactory.decodeByteArray(this.mAvatarData, 0,
						this.mAvatarData.length);
				this.mAvatar = new BitmapDrawable(context.getResources(), b);
			}
		}
		if (this.mAvatar == null) {
			return defaultValue;
		}
		return this.mAvatar;
	}
}
