import 'package:flutter/cupertino.dart';
import 'package:flutter/services.dart';

typedef void StringCallback(String val);

class Nfc {

  static const platform = const MethodChannel('nfchelper');
  final StringCallback callback;
  String _cardNumber = 'Waiting...';

  Nfc({this.callback}) {
    platform.setMethodCallHandler(_handleCardRead);
  }

  String getCardNumber() => _cardNumber;

  // Call to reset nfc and prepare reading
  Future<void> startReading() async {
    try {
      final String result = await platform.invokeMethod('getCardNumber');
      _cardNumber = result;
    } on PlatformException catch (e) {
      _cardNumber = "Failed to get card number: '${e.message}'.";
    }
  }

  // handler which is called when nfc reader status is changed in any way, i.e.:
  // card is within range, card read error, or card read is ok
  Future<dynamic> _handleCardRead(MethodCall call) async {
    switch(call.method) {
      case "backCardNumber":
        debugPrint(call.arguments);
        callback(call.arguments);
        return new Future.value(call.arguments);
    }
  }

}
