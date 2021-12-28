rm -rf build/package
./gradlew build
./gradlew launch4j
mkdir build/package
cp -r build/launch4j/lib build/package/.
cp -r build/launch4j/CardsOfLegendsGame.exe build/package/.
cp -r resources build/package/.