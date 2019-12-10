package com.exponea.sdk.models

import com.google.gson.Gson
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class InAppMessageTest {
    val json = """
    {
        "id": "5dd86f44511946ea55132f29",
        "name": "Test serving in-app message",
        "message_type": "modal",
        "frequency": "only_once",
        "payload": {
            "image_url":"https://i.ytimg.com/vi/t4nM1FoUqYs/maxresdefault.jpg",
            "title":"filip.vozar@exponea.com",
            "title_text_color":"#000000",
            "title_text_size":"22px",
            "body_text":"This is an example of your in-app message body text.",
            "body_text_color":"#000000",
            "body_text_size":"14px",
            "button_text":"Action",
            "button_type":"deep-link",
            "button_link":"https://someaddress.com",
            "button_text_color":"#ffffff",
            "button_background_color":"#f44cac",
            "background_color":"#ffffff",
            "close_button_color":"#ffffff"
        },
        "variant_id": 0,
        "variant_name": "Variant A",
        "trigger": {
            "type": "event",
            "event_type": "session_start"
        },
        "date_filter": {
            "enabled": false,
            "from_date": null,
            "to_date": null
        }
    }
    """

    @Test
    fun `should parse in-app message from json`() {
        assertEquals(
            InAppMessage(
                id = "5dd86f44511946ea55132f29",
                name = "Test serving in-app message",
                messageType = "modal",
                frequency = "only_once",
                variantId = 0,
                variantName = "Variant A",
                trigger = InAppMessageTrigger(type = "event", eventType = "session_start"),
                dateFilter = DateFilter(false, null, null),
                payload = InAppMessagePayload(
                    imageUrl = "https://i.ytimg.com/vi/t4nM1FoUqYs/maxresdefault.jpg",
                    title = "filip.vozar@exponea.com",
                    titleTextColor = "#000000",
                    titleTextSize = "22px",
                    bodyText = "This is an example of your in-app message body text.",
                    bodyTextColor = "#000000",
                    bodyTextSize = "14px",
                    buttonText = "Action",
                    buttonType = "deep-link",
                    buttonLink = "https://someaddress.com",
                    buttonTextColor = "#ffffff",
                    buttonBackgroundColor = "#f44cac",
                    backgroundColor = "#ffffff",
                    closeButtonColor = "#ffffff"
                )
            ),
            Gson().fromJson(json, InAppMessage::class.java)
        )
    }
}
