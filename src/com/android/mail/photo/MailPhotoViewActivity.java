package com.android.mail.photo;

import android.app.ActionBar;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.ex.photo.PhotoViewActivity;
import com.android.mail.R;
import com.android.mail.browse.AttachmentActionHandler;
import com.android.mail.providers.Attachment;
import com.android.mail.providers.UIProvider.AttachmentDestination;
import com.android.mail.utils.AttachmentUtils;
import com.android.mail.utils.Utils;

import java.util.ArrayList;

/**
 * Derives from {@link PhotoViewActivity} to allow customization
 * to the {@link ActionBar} from the default implementation.
 */
public class MailPhotoViewActivity extends PhotoViewActivity {
    private MenuItem mSaveItem;
    private AttachmentActionHandler mActionHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionHandler = new AttachmentActionHandler(this, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.photo_view_menu, menu);

        mSaveItem = menu.findItem(R.id.menu_save);

        // Turn off the functionality that only works on JellyBean.
        menu.findItem(R.id.menu_share)
                .setVisible(Utils.isRunningJellybeanOrLater());
        menu.findItem(R.id.menu_share_all)
                .setVisible(Utils.isRunningJellybeanOrLater());

        updateActionItems();

        return true;
    }

    /**
     * Updates the action items to tweak their visibility in case
     * there is functionality thati is not relevant (eg, the Save
     * button should not appear if the photo has already been saved).
     */
    private void updateActionItems() {
        final Attachment attachment = getCurrentAttachment();
        if (attachment != null) {
            final boolean isDownloading = attachment.isDownloading();
            final boolean isSavedToExternal = attachment.isSavedToExternal();
            final boolean canSave = attachment.canSave();
            mSaveItem.setVisible(!isDownloading && canSave && !isSavedToExternal);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go back to conversation
                finish();
                return true;
            case R.id.menu_save: // save the current photo
                saveAttachment();
                return true;
            case R.id.menu_save_all: // save all of the photos
                saveAllAttachments();
                return true;
            case R.id.menu_share: // share the current photo
                shareAttachment();
                return true;
            case R.id.menu_share_all: // share all of the photos
                shareAllAttachments();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Adjusts the activity title and subtitle to reflect the image name and size.
     */
    @Override
    protected void updateActionBar() {
        super.updateActionBar();

        final Attachment attachment = getCurrentAttachment();
        final boolean isDownloading = attachment.isDownloading();
        final boolean isSavedToExternal = attachment.isSavedToExternal();

        final ActionBar actionBar = getActionBar();
        String subtitle =
                AttachmentUtils.convertToHumanReadableSize(this, attachment.size);

        if (isSavedToExternal) {
            subtitle = getResources().getString(R.string.saved) + " " + subtitle;
            actionBar.setSubtitle(subtitle);
        } else if (isDownloading) {
            actionBar.setSubtitle(R.string.saving);
        } else {
            actionBar.setSubtitle(subtitle);
        }

        updateActionItems();
    }

    /**
     * Save the current attachment.
     */
    private void saveAttachment() {
        saveAttachment(getCurrentAttachment());
    }

    /**
     * Saves the attachment.
     * @param attachment the attachment to save.
     */
    private void saveAttachment(final Attachment attachment) {
        if (attachment != null && attachment.canSave()) {
            mActionHandler.setAttachment(attachment);
            mActionHandler.startDownloadingAttachment(AttachmentDestination.EXTERNAL);
        }
    }

    /**
     * Save all of the attachments in the cursor.
     */
    private void saveAllAttachments() {
        Cursor cursor = getCursorAtProperPosition();

        if (cursor == null) {
            return;
        }

        int i = -1;
        while (cursor.moveToPosition(++i)) {
            saveAttachment(new Attachment(cursor));
        }
    }

    /**
     * Share the current attachment.
     */
    private void shareAttachment() {
        shareAttachment(getCurrentAttachment());
    }

    /**
     * Shares the attachment
     * @param attachment the attachment to share
     */
    private void shareAttachment(final Attachment attachment) {
        if (attachment != null) {
            mActionHandler.setAttachment(attachment);
            mActionHandler.shareAttachment();
        }
    }

    /**
     * Share all of the attachments in the cursor.
     */
    private void shareAllAttachments() {
        Cursor cursor = getCursorAtProperPosition();

        if (cursor == null) {
            return;
        }

        ArrayList<Parcelable> uris = new ArrayList<Parcelable>();
        int i = -1;
        while (cursor.moveToPosition(++i)) {
            uris.add(Utils.normalizeUri(new Attachment(cursor).contentUri));
        }

        mActionHandler.shareAttachments(uris);
    }

    /**
     * Helper method to get the currently visible attachment.
     * @return
     */
    private Attachment getCurrentAttachment() {
        final Cursor cursor = getCursorAtProperPosition();

        if (cursor == null) {
            return null;
        }

        return new Attachment(cursor);
    }
}