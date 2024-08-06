/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.calendar.test.util;

import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.ServiceContextTestUtil;
import com.liferay.portal.kernel.util.CalendarFactoryUtil;

/**
 * @author Richard Jeremias
 */
public class CalendarBookingUpgradeProcessTestUtil {

	public static CalendarBooking createCalendarBooking(
			User user, UserLocalService userLocalService)
		throws Exception {

		Group group = GroupTestUtil.addGroup();

		Calendar calendar = CalendarTestUtil.addCalendar(group);

		ServiceContext serviceContext =
			ServiceContextTestUtil.getServiceContext(
				group.getGroupId(), user.getUserId());

		user.setTimeZoneId("Europe/Paris");

		user.setGroupIds(new long[] {group.getGroupId()});

		user = userLocalService.updateUser(user);

		java.util.Calendar startTimeJCalendar = CalendarFactoryUtil.getCalendar(
			2022, java.util.Calendar.JANUARY, 1, 0, 0);

		java.util.Calendar endTimeJCalendar = CalendarFactoryUtil.getCalendar(
			2022, java.util.Calendar.JANUARY, 1, 23, 59);

		return CalendarBookingTestUtil.addAllDayCalendarBooking(
			user, calendar, startTimeJCalendar.getTimeInMillis(),
			endTimeJCalendar.getTimeInMillis(), serviceContext);
	}

	public static String getClassName(String version) {
		return "com.liferay.calendar.internal.upgrade." + version +
			".CalendarBookingUpgradeProcess";
	}

}