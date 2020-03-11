import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'nfc.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'Flutter NFC reader'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);

  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {

  Nfc nfc;
  String _cardNumber = 'Press button to read';

  @override
  void initState() {
    nfc = new Nfc(callback: (val) => setState(() => _cardNumber = val));
    super.initState();
  }

  Future<void> _getCardNumber() async {
    nfc.startReading();
    setState(() {
      _cardNumber = nfc.getCardNumber();
    });
  }

    @override Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Container(
              padding: EdgeInsets.fromLTRB(10, 10, 10, 10),
              child: Text(
                'Card number:',
                style: Theme.of(context).textTheme.title,
              ),
            ),
            Container(
              padding: EdgeInsets.fromLTRB(10, 10, 10, 10),
              color: Theme.of(context).secondaryHeaderColor,
              child: Text(
                '$_cardNumber',
                style: Theme.of(context).textTheme.title,
              ),
            ),
            RaisedButton(
              onPressed: () => _getCardNumber(),
              child: Text('Start reading'),
            )
          ],
        ),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }
}
