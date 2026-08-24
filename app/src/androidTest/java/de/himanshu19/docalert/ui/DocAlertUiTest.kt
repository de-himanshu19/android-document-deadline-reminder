package de.himanshu19.docalert.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.himanshu19.docalert.data.settings.AppSettings
import de.himanshu19.docalert.domain.model.Category
import de.himanshu19.docalert.domain.model.ItemDraft
import de.himanshu19.docalert.domain.model.ItemQuery
import de.himanshu19.docalert.domain.model.ItemType
import de.himanshu19.docalert.domain.model.TrackedItem
import de.himanshu19.docalert.domain.model.ValidationErrors
import de.himanshu19.docalert.ui.details.DetailsScreen
import de.himanshu19.docalert.ui.details.DetailsUiState
import de.himanshu19.docalert.ui.editor.EditorScreen
import de.himanshu19.docalert.ui.editor.EditorUiState
import de.himanshu19.docalert.ui.home.HomeScreen
import de.himanshu19.docalert.ui.home.HomeUiState
import de.himanshu19.docalert.ui.settings.SettingsScreen
import de.himanshu19.docalert.ui.settings.SettingsUiState
import de.himanshu19.docalert.ui.theme.DocAlertTheme
import de.himanshu19.docalert.ui.testing.ComposeTestActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class DocAlertUiTest {
    @get:Rule val compose = createAndroidComposeRule<ComposeTestActivity>()

    @Test fun emptyDashboardAndAddActionAreAccessible() {
        var added = false
        compose.setContent { DocAlertTheme(de.himanshu19.docalert.data.settings.ThemeMode.LIGHT) {
            HomeScreen(HomeUiState(false), {}, {}, {}, {}, { added = true }, {}, {})
        } }
        compose.onNodeWithText("Never miss an important date").assertIsDisplayed()
        compose.onNodeWithText("Add Document or Deadline").performClick()
        compose.runOnIdle { assertTrue(added) }
    }

    @Test fun savedCardRendersStatusSemanticsAndSearchEmitsInput() {
        val item = TrackedItem(1, ItemType.DOCUMENT, "Passport", Category.PASSPORT, expiryDate = LocalDate.now().plusDays(20), createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH)
        var search by mutableStateOf("")
        compose.setContent { DocAlertTheme(de.himanshu19.docalert.data.settings.ThemeMode.LIGHT) {
            HomeScreen(HomeUiState(false, listOf(item), ItemQuery(search = search)), { search = it }, {}, {}, {}, {}, {}, {})
        } }
        compose.onNodeWithText("Passport").assertIsDisplayed()
        compose.onNodeWithContentDescription("Passport, Urgent, 20 days remaining").assertIsDisplayed()
        compose.onNodeWithText("Search saved items").performTextInput("pass")
        compose.runOnIdle { assertTrue(search.contains("pass")) }
    }

    @Test fun editorShowsFieldValidationWithoutLosingText() {
        compose.setContent { DocAlertTheme(de.himanshu19.docalert.data.settings.ThemeMode.LIGHT) {
            EditorScreen(EditorUiState(false, ItemDraft(title = "Entered"), ValidationErrors(title = "Enter a title."), true), false, {}, {}, {})
        } }
        compose.onNodeWithText("Entered").assertIsDisplayed()
        compose.onNodeWithText("Enter a title.").assertIsDisplayed()
    }

    @Test fun detailsRequiresDeleteConfirmation() {
        val item = TrackedItem(1, ItemType.DOCUMENT, "Passport", Category.PASSPORT, expiryDate = LocalDate.now(), createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH)
        compose.setContent { DocAlertTheme(de.himanshu19.docalert.data.settings.ThemeMode.LIGHT) {
            DetailsScreen(DetailsUiState(false, item), {}, {}, {})
        } }
        compose.onNodeWithContentDescription("Delete item").performClick()
        compose.onNodeWithText("Delete this item?").assertIsDisplayed()
    }

    @Test fun settingsExposesThemeAndPrivacyControls() {
        compose.setContent { DocAlertTheme(de.himanshu19.docalert.data.settings.ThemeMode.LIGHT) {
            SettingsScreen(SettingsUiState(false, AppSettings()), false, {}, {}, {}, {}, {}, {}, {})
        } }
        compose.onNodeWithText("System default").assertIsDisplayed()
        compose.onNodeWithText("Private notification content").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Open notification settings").assertIsDisplayed()
    }
}
