/*
 * This is the source code of Emm for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package info.emm.ui.Views;

import info.emm.objects.MessageObject;

public abstract class AbstractGalleryActivity extends PausableActivity {
	public abstract void topBtn();
    public abstract void didShowMessageObject(MessageObject obj);
}