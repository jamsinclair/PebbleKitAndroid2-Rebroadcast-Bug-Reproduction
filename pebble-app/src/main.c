#include <pebble.h>

// Layer positioning constants
#define PING_LAYER_Y 40
#define PONG_LAYER_Y 90
#define LAYER_HEIGHT 60

// Global state
static Window *s_window;
static TextLayer *s_ping_layer;
static TextLayer *s_pong_layer;
static bool s_ping_sent = false;
static bool s_pong_received = false;

// Forward declarations
static void init_app_message(void);
static void deinit_app_message(void);
static void update_display(void);
static TextLayer* create_text_layer(GRect bounds, int y_offset, const char *text);

// AppMessage Callbacks
static void inbox_received_handler(DictionaryIterator *iterator, void *context) {
  APP_LOG(APP_LOG_LEVEL_INFO, "Message received");

  Tuple *pong_tuple = dict_find(iterator, MESSAGE_KEY_pong);
  if (pong_tuple) {
    APP_LOG(APP_LOG_LEVEL_INFO, "Pong received");
    s_pong_received = true;
    update_display();
  }
}

static void outbox_failed_handler(DictionaryIterator *iterator, AppMessageResult reason, void *context) {
  APP_LOG(APP_LOG_LEVEL_ERROR, "Outbox failed: %d", reason);
}

static void init_app_message(void) {
  APP_LOG(APP_LOG_LEVEL_INFO, "Initializing AppMessage");

  app_message_register_inbox_received(inbox_received_handler);
  app_message_register_outbox_failed(outbox_failed_handler);

  AppMessageResult result = app_message_open(128, 128);
  APP_LOG(APP_LOG_LEVEL_INFO, "app_message_open result: %d", result);
}

static void deinit_app_message(void) {
  app_message_deregister_callbacks();
}

static void update_display(void) {
  layer_set_hidden(text_layer_get_layer(s_ping_layer), !s_ping_sent);
  layer_set_hidden(text_layer_get_layer(s_pong_layer), !s_pong_received);
}

static TextLayer* create_text_layer(GRect bounds, int y_offset, const char *text) {
  TextLayer *layer = text_layer_create(GRect(0, y_offset, bounds.size.w, LAYER_HEIGHT));
  text_layer_set_text(layer, text);
  text_layer_set_text_alignment(layer, GTextAlignmentCenter);
  text_layer_set_font(layer, fonts_get_system_font(FONT_KEY_GOTHIC_28_BOLD));
  return layer;
}

static void select_click_handler(ClickRecognizerRef recognizer, void *context) {
  APP_LOG(APP_LOG_LEVEL_INFO, "Select button pressed");

  DictionaryIterator *iter;
  AppMessageResult begin_result = app_message_outbox_begin(&iter);

  if (begin_result != APP_MSG_OK) {
    APP_LOG(APP_LOG_LEVEL_ERROR, "Failed to begin outbox: %d", begin_result);
    return;
  }

  dict_write_int(iter, MESSAGE_KEY_ping, &(int){1}, sizeof(int), true);

  AppMessageResult send_result = app_message_outbox_send();
  if (send_result == APP_MSG_OK) {
    APP_LOG(APP_LOG_LEVEL_INFO, "Ping sent successfully");
    s_ping_sent = true;
    update_display();
  } else {
    APP_LOG(APP_LOG_LEVEL_ERROR, "Failed to send ping: %d", send_result);
  }
}

static void click_config_provider(void *context) {
  window_single_click_subscribe(BUTTON_ID_SELECT, select_click_handler);
}

static void window_load(Window *window) {
  Layer *window_layer = window_get_root_layer(window);
  GRect bounds = layer_get_bounds(window_layer);

  s_ping_layer = create_text_layer(bounds, PING_LAYER_Y, "ping");
  layer_add_child(window_layer, text_layer_get_layer(s_ping_layer));

  s_pong_layer = create_text_layer(bounds, PONG_LAYER_Y, "pong");
  layer_add_child(window_layer, text_layer_get_layer(s_pong_layer));

  update_display();
}

static void window_unload(Window *window) {
  text_layer_destroy(s_ping_layer);
  text_layer_destroy(s_pong_layer);
}

static void init(void) {
  init_app_message();

  s_window = window_create();
  window_set_click_config_provider(s_window, click_config_provider);
  window_set_window_handlers(s_window, (WindowHandlers) {
    .load = window_load,
    .unload = window_unload,
  });
  window_stack_push(s_window, true);

  APP_LOG(APP_LOG_LEVEL_INFO, "App initialized");
}

static void deinit(void) {
  deinit_app_message();
  window_destroy(s_window);
}

int main(void) {
  init();
  app_event_loop();
  deinit();
  return 0;
}
