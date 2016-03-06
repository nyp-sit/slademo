module.exports = {
  'name': 'number',
  'category': 'Type',
  'syntax': [
    'x',
    'number(x)'
  ],
  'description':
      'Create a number or convert a string or boolean into a number.',
  'examples': [
    '2',
    '2e3',
    '4.05',
    'number(2)',
    'number("7.2")',
    'number(true)',
    'number([true, false, true, true])',
    'number("52cm", "m")'
  ],
  'seealso': [
    'bignumber', 'boolean', 'complex', 'fraction', 'index', 'matrix', 'string', 'unit'
  ]
};
