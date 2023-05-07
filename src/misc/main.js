const fs = require('fs');
const { createCanvas, loadImage, registerFont } = require('canvas');
registerFont('Oswald-Regular.ttf', { family: 'Oswald Regular' });
registerFont('Military Poster.ttf', { family: 'Military Poster Regular' });
const canvas = createCanvas(688, 276);
const ctx = canvas.getContext('2d')
const colorsRanks = {
  'Noob': "#BDBDBD",
  'Lucker': "#00ce21",
  'Strawberry Legend': "#ff8413",
  'Drunken Master': "#13f1ff",
  'Rambo': "#c8ff2c",
  'Terminator': "#009fd1",
  'Terminator 2': "#2c5aff",
  'Professional': "#a513ff",
  'Legend': "#ffd600",
  'Nanaxer': "#d00a0a",
};

function makeContextSecondColumn() {
  ctx.fillStyle = '#d00a0a'
  ctx.globalAlpha = '1';
}

function makeContextFirstColumn() {
  ctx.fillStyle = 'white'
  ctx.globalAlpha = '1';
}



loadImage('nanax_logo.png').then((image) => {
  ctx.fillStyle = 'black'
  ctx.fillRect(0, 0, canvas.width, canvas.height)
  ctx.globalAlpha = '0.22';
  ctx.drawImage(image, 50, 0, image.naturalWidth, image.naturalHeight)
  ctx.globalAlpha = '1';
  ctx.font = '28px Oswald';
  ctx.fillStyle = 'white'



  makeContextFirstColumn();
  ctx.fillText('Country', 230, 55)
  makeContextSecondColumn();
  ctx.fillText('Switzerland', 326, 55)

  makeContextFirstColumn();
  ctx.fillText('Tag', 230, 90)
  makeContextSecondColumn();
  ctx.fillText('Navi', 326, 90)

  makeContextFirstColumn();
  ctx.fillText('Wins', 488, 55)
  makeContextSecondColumn();
  ctx.fillText('134534', 570, 55)

  makeContextFirstColumn();
  ctx.fillText('Losses', 488, 90)
  makeContextSecondColumn();
  ctx.fillText('345', 570, 90)

  makeContextFirstColumn();
  ctx.fillText('Draws', 488, 125)
  makeContextSecondColumn();
  ctx.fillText('345', 570, 125)

  makeContextFirstColumn();
  ctx.fillText('Total', 488, 160)
  makeContextSecondColumn();
  ctx.fillText('345343', 570, 160)

  makeContextFirstColumn();
  ctx.fillText('Win Rate', 488, 210)
  makeContextSecondColumn();
  ctx.fillText('58%', 595, 210)

  makeContextFirstColumn();
  ctx.fillText('Points', 230, 210)
  makeContextSecondColumn();
  ctx.fillText('332.32', 326, 210)
  ctx.fillStyle = colorsRanks['Strawberry Legend'];
  ctx.fillText('"Strawberry Legend"', 230, 245)

  makeContextFirstColumn();
  ctx.fillText('NANAX Points', 488, 245)
  makeContextSecondColumn();
  ctx.fillText('6', 643, 245)
  ctx.stroke();

}).then(() => {
  loadImage('avatar2.jpeg').then((image) => {
    ctx.globalAlpha = '1';
    ctx.drawImage(image, 32, 31, 128, 128);
    ctx.font = '57px "Military Poster"';
    ctx.fillStyle = colorsRanks['Strawberry Legend'];
    ctx.fillText('macautribes', 60, 175)
    ctx.fillStyle = 'white';
    ctx.font = '43px "Oswald"';
    ctx.fillText('#153', 32, 245)
    const imgBuffer = canvas.toBuffer('image/png')
    fs.writeFileSync('test.png', imgBuffer)
  });
});
