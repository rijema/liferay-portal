/**
 * SPDX-FileCopyrightText: (c) 2024 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

import {expect, mergeTests} from '@playwright/test';

import {apiHelpersTest} from '../../fixtures/apiHelpersTest';
import {loginTest} from '../../fixtures/loginTest';
import {objectPagesTest} from '../../fixtures/objectPagesTest';
import {getRandomInt} from '../../utils/getRandomInt';

export const test = mergeTests(apiHelpersTest, loginTest(), objectPagesTest);

test.describe('Manage object fields through Model Builder', () => {
	test('can add picklist object field to object definition node', async ({
		apiHelpers,
		modelBuilderPage,
		page,
		viewObjectDefinitionsPage,
	}) => {
		await page.goto('/');

		const listTypeDefinition =
			await apiHelpers.listTypeAdmin.postRandomListTypeDefinition();

		const objectDefinition =
			await apiHelpers.objectAdmin.postRandomObjectDefinition('default');

		await viewObjectDefinitionsPage.goto();

		await viewObjectDefinitionsPage.openObjectFolder('default');

		await viewObjectDefinitionsPage.viewInModelBuilder();

		const objectFieldLabel = 'objectFieldLabel' + getRandomInt();

		await modelBuilderPage.createObjectField({
			listTypeDefinitionName: listTypeDefinition.name,
			mandatory: false,
			objectDefinitionName: objectDefinition.name,
			objectFieldBusinessType: 'Picklist',
			objectFieldLabel,
		});

		await expect(
			modelBuilderPage.objectDefinitionNodes
				.filter({hasText: objectDefinition.name})
				.getByText(objectFieldLabel)
		).toBeVisible();

		// Clean up

		await apiHelpers.objectAdmin.deleteObjectDefinition(
			objectDefinition.id
		);

		await apiHelpers.listTypeAdmin.deleteListTypeDefinition(
			listTypeDefinition.id
		);
	});

	test('all picklist definitions are listed during object field creation', async ({
		apiHelpers,
		modelBuilderPage,
		page,
		viewObjectDefinitionsPage,
	}) => {
		const listTypeDefinitions = await Promise.all(
			Array(22)
				.fill(null)
				.map(() =>
					apiHelpers.listTypeAdmin.postRandomListTypeDefinition()
				)
		);

		const objectDefinition =
			await apiHelpers.objectAdmin.postRandomObjectDefinition('default');

		try {
			await page.goto('/');

			await viewObjectDefinitionsPage.goto();

			await viewObjectDefinitionsPage.openObjectFolder('default');

			await viewObjectDefinitionsPage.viewInModelBuilder();

			await modelBuilderPage.openObjectFieldSelectionPage(
				objectDefinition.name,
				'Picklist',
				'objectFieldLabel' + getRandomInt()
			);

			modelBuilderPage.newObjectFieldSelectPicklist.click();

			const listTypeDefinitionBox =
				modelBuilderPage.page.getByRole('listbox');

			await expect(listTypeDefinitionBox).toBeVisible();

			await expect(
				listTypeDefinitionBox.getByRole('listitem')
			).toHaveCount(22);
		}
		finally {

			// Clean up

			await apiHelpers.objectAdmin.deleteObjectDefinition(
				objectDefinition.id
			);

			await Promise.all(
				listTypeDefinitions.map((listTypeDefinition) =>
					apiHelpers.listTypeAdmin.deleteListTypeDefinition(
						listTypeDefinition.id
					)
				)
			);
		}
	});
});
