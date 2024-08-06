/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.calendar.upgrade.v4_2_1.test;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.service.CalendarBookingLocalService;
import com.liferay.calendar.service.CalendarLocalService;
import com.liferay.calendar.test.util.CalendarBookingTestUtil;
import com.liferay.calendar.test.util.CalendarTestUtil;
import com.liferay.calendar.test.util.CalendarUpgradeTestUtil;
import com.liferay.calendar.test.util.UpgradeDatabaseTestHelper;
import com.liferay.calendar.util.JCalendarUtil;
import com.liferay.change.tracking.test.util.BaseCTUpgradeProcessTestCase;
import com.liferay.portal.kernel.dao.orm.EntityCacheUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.change.tracking.CTModel;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.change.tracking.CTService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.upgrade.UpgradeProcess;
import com.liferay.portal.kernel.util.CalendarFactoryUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;
import com.liferay.portal.test.rule.PermissionCheckerMethodTestRule;
import com.liferay.portal.upgrade.registry.UpgradeStepRegistrator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author István András Dézsi
 */
@RunWith(Arquillian.class)
public class UpgradeCalendarBookingTest extends BaseCTUpgradeProcessTestCase {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new AggregateTestRule(
			new LiferayIntegrationTestRule(),
			PermissionCheckerMethodTestRule.INSTANCE);

	@ClassRule
	@Rule
	public static final LiferayIntegrationTestRule liferayIntegrationTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_calendar = CalendarTestUtil.addCalendar(_group);

		_upgradeDatabaseTestHelper =
			CalendarUpgradeTestUtil.getUpgradeDatabaseTestHelper();
		_upgradeProcess = CalendarUpgradeTestUtil.getUpgradeStep(
			_upgradeStepRegistrator, _CLASS_NAME);
		_user = UserTestUtil.addUser();

		_serviceContext = createServiceContext();
	}

	@After
	public void tearDown() throws Exception {
		_upgradeDatabaseTestHelper.close();
	}

	@Test
	public void testUpgradeAllDayCalendarBookingStartAndEndTime()
		throws Exception {

		CalendarBooking calendarBooking = createCalendarBooking();

		java.util.Calendar expectedStartTimeJCalendar =
			JCalendarUtil.getJCalendar(calendarBooking.getStartTime());

		java.util.Calendar expectedEndTimeJCalendar =
			JCalendarUtil.getJCalendar(calendarBooking.getEndTime());

		_upgradeProcess.upgrade();

		EntityCacheUtil.clearCache();

		calendarBooking = _calendarBookingLocalService.getCalendarBooking(
			calendarBooking.getCalendarBookingId());

		java.util.Calendar actualStartTimeJCalendar =
			JCalendarUtil.getJCalendar(calendarBooking.getStartTime());

		assertSameTime(expectedStartTimeJCalendar, actualStartTimeJCalendar);

		java.util.Calendar actualEndTimeJCalendar = JCalendarUtil.getJCalendar(
			calendarBooking.getEndTime());

		assertSameTime(expectedEndTimeJCalendar, actualEndTimeJCalendar);
	}

	@Override
	protected CTModel<?> addCTModel() throws Exception {
		return createCalendarBooking();
	}

	protected void assertSameTime(
		java.util.Calendar expectedJCalendar,
		java.util.Calendar actualJCalendar) {

		Assert.assertNotNull(expectedJCalendar);
		Assert.assertNotNull(actualJCalendar);
		Assert.assertEquals(
			expectedJCalendar.get(java.util.Calendar.HOUR),
			actualJCalendar.get(java.util.Calendar.HOUR));
		Assert.assertEquals(
			expectedJCalendar.get(java.util.Calendar.MINUTE),
			actualJCalendar.get(java.util.Calendar.MINUTE));
	}

	protected CalendarBooking createCalendarBooking() throws PortalException {
		setUserTimeZoneId();

		java.util.Calendar expectedStartTimeJCalendar =
			CalendarFactoryUtil.getCalendar(
				2022, java.util.Calendar.JANUARY, 1, 0, 0);

		java.util.Calendar expectedEndTimeJCalendar =
			CalendarFactoryUtil.getCalendar(
				2022, java.util.Calendar.JANUARY, 1, 23, 59);

		return CalendarBookingTestUtil.addAllDayCalendarBooking(
			_user, _calendar, expectedStartTimeJCalendar.getTimeInMillis(),
			expectedEndTimeJCalendar.getTimeInMillis(), _serviceContext);
	}

	protected ServiceContext createServiceContext() {
		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setCompanyId(_user.getCompanyId());
		serviceContext.setUserId(_user.getUserId());

		return serviceContext;
	}

	@Override
	protected CTService<?> getCTService() {
		return _calendarBookingLocalService;
	}

	@Override
	protected void runUpgrade() throws Exception {
		_upgradeProcess.upgrade();
	}

	protected void setUserTimeZoneId() {
		_user.setTimeZoneId("Europe/Paris");

		_userLocalService.updateUser(_user);
	}

	@Override
	protected CTModel<?> updateCTModel(CTModel<?> ctModel) {
		_calendarBooking = (CalendarBooking)ctModel;

		java.util.Calendar startTimeJCalendar = CalendarFactoryUtil.getCalendar(
			2022, java.util.Calendar.JANUARY, 1, 23, 0);

		java.util.Calendar endTimeJCalendar = CalendarFactoryUtil.getCalendar(
			2022, java.util.Calendar.JANUARY, 2, 22, 59);

		_calendarBooking.setStartTime(startTimeJCalendar.getTimeInMillis());
		_calendarBooking.setEndTime(endTimeJCalendar.getTimeInMillis());

		_calendarBooking = _calendarBookingLocalService.updateCalendarBooking(
			_calendarBooking);

		return _calendarBooking;
	}

	private static final String _CLASS_NAME =
		"com.liferay.calendar.internal.upgrade.v4_2_1." +
			"CalendarBookingUpgradeProcess";

	private Calendar _calendar;

	@DeleteAfterTestRun
	private CalendarBooking _calendarBooking;

	@Inject
	private CalendarBookingLocalService _calendarBookingLocalService;

	@Inject
	private CalendarLocalService _calendarLocalService;

	private Group _group;
	private ServiceContext _serviceContext;
	private UpgradeDatabaseTestHelper _upgradeDatabaseTestHelper;
	private UpgradeProcess _upgradeProcess;

	@Inject(
		filter = "component.name=com.liferay.calendar.internal.upgrade.registry.CalendarServiceUpgradeStepRegistrator"
	)
	private UpgradeStepRegistrator _upgradeStepRegistrator;

	private User _user;

	@Inject
	private UserLocalService _userLocalService;

}