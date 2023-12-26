/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2023 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package org.voidsink.anewjkuapp.mock;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class DummyLogin implements IDummyLogin {

    private static final String USER = "google";
    private static final String PASSWORD = "ThisIsAMockBecauseOfGooglePolicyWhichSuxx";
    private static final String SESSION_ID = " e1f8f2c4-70ed-4c39-908e-89c3afc850e1";

    @Override
    public boolean isGoogleLogin(String user, String password) {
        return USER.equals(user) && PASSWORD.equals(password);
    }

    @Override
    public String getSessionId() {
        return SESSION_ID;
    }

    @Override
    public boolean isGoogleSession(String sessionId) {
        return SESSION_ID.equals(sessionId);
    }

    @Override
    public void prepareCookies(CookieStore cookieStore, String url) {
        try {
            List<HttpCookie> cookies = cookieStore.get(new URI(url));
            cookies.add(new HttpCookie("JSESSIONID", getSessionId()));
        } catch (URISyntaxException ignored) {
        }
    }
}
