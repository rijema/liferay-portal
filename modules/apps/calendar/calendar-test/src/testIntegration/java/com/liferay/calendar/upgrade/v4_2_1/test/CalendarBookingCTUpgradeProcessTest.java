/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.calendar.upgrade.v4_2_1.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.service.CalendarBookingLocalService;
import com.liferay.calendar.test.util.CalendarBookingUpgradeProcessTestUtil;
import com.liferay.calendar.test.util.CalendarUpgradeTestUtil;
import com.liferay.change.tracking.test.util.BaseCTUpgradeProcessTestCase;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.change.tracking.CTModel;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.change.tracking.CTService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.CalendarFactoryUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import java.util.Calendar;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * @author Richard Jeremias
 */
@RunWith(Arquillian.class)
public class CalendarBookingCTUpgradeProcessTest
	extends BaseCTUpgradeProcessTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@Before
	public void setUp() throws Exception {
		_upgradeProcess = CalendarUpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator,
			CalendarBookingUpgradeProcessTestUtil.getClassName("v4_2_1"));
		_user = UserTestUtil.addUser();
	}

	@Override
	protected CTModel<?> addCTModel() throws Exception {
		return CalendarBookingUpgradeProcessTestUtil.createCalendarBooking(
			_user, _userLocalService);
	}

	@Override
	protected CTService<?> getCTService() {
		return _calendarBookingLocalService;
	}

	@Override
	protected void runUpgrade() throws Exception {
		_upgradeProcess.upgrade();
	}

	@Override
	protected CTModel<?> updateCTModel(CTModel<?> ctModel) {
		_calendarBooking = (CalendarBooking)ctModel;

		Calendar startTimeJCalendar = CalendarFactoryUtil.getCalendar(
			2022, Calendar.JANUARY, 1, 23, 0);

		Calendar endTimeJCalendar = CalendarFactoryUtil.getCalendar(
			2022, Calendar.JANUARY, 2, 22, 59);

		_calendarBooking.setStartTime(startTimeJCalendar.getTimeInMillis());
		_calendarBooking.setEndTime(endTimeJCalendar.getTimeInMillis());

		_calendarBooking = _calendarBookingLocalService.updateCalendarBooking(
			_calendarBooking);

		return _calendarBooking;
	}

	@DeleteAfterTestRun
	private CalendarBooking _calendarBooking;

	@Inject
	private CalendarBookingLocalService _calendarBookingLocalService;

	private UpgradeProcess _upgradeProcess;

	@Inject(
		filter = "component.name=com.liferay.calendar.internal.upgrade.registry.CalendarServiceUpgradeStepRegistrator"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}